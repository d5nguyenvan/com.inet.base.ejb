/*****************************************************************
   Copyright 2006 by Dung Nguyen (dungnguyen@truthinet.com)

   Licensed under the iNet Solutions Corp.,;
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.truthinet.com/licenses

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*****************************************************************/
package com.inet.base.ejb.internal.ref;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.inet.base.ejb.internal.Preconditions;

/**
 * ReferenceHashMap.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: ReferenceHashMap.java 2009-07-29 16:08:55z nguyen_dv $
 *
 * @since 1.0
 */
public abstract class ReferenceHashMap<K, V> extends AbstractMap<K, V>
  implements Map<K, V>, Cloneable, Serializable {

  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = -2252754239641773996L;

  /**
   * The default initial capacity -- MUST be a power of two.
   */
  private static final int DEFAULT_INITIAL_CAPACITY = 16;

  /**
   * The maximum capacity, used if a higher value is implicitly specified
   * by either of the constructors with arguments.
   * MUST be a power of two <= 1<<30.
   */
  private static final int MAXIMUM_CAPACITY = 1 << 30;

  /**
   * The load fast used when none specified in constructor.
   */
  private static final float DEFAULT_LOAD_FACTOR = 0.75f;

  /**
   * the table, resized as necessary. Length MUST always be a power of two.
   */
  @SuppressWarnings({ "unchecked" })
  private transient Entry[] table;

  /**
   * The number of key-value mappings contained in this weak hash map.
   */
  private transient int size;

  /**
   * The next size value at which to resize (capacity * load factor).
   */
  private int threshold;

  /**
   * The load factor for the hash table.
   */
  private final float loadFactor;

  /**
   * Reference queue for cleared WeakEntries.
   */
  private transient ReferenceQueue<K> queue = new ReferenceQueue<K>();

  /**
   * The number of times this HashMap has been structurally modified
   * Structural modifications are those that change the number of mappings in
   * the HashMap or otherwise modify its internal structure (e.g.,
   * rehash).  This field is used to make iterators on Collection-views of
   * the HashMap fail-fast.  (See ConcurrentModificationException).
   */
  private transient volatile int modCount;

  /**
   * Create the instance from the given initial capacity and load factor value.
   *
   * @param initialCapacity the given initial capacity.
   * @param lf              the load factor of the hash map.
   * @exception IllegalArgumentException if the initial capacity is negative, or if the load factor
   *            is nonpositive.
   */
  protected ReferenceHashMap(int initialCapacity, final float lf) {
    Preconditions.checkArgument((initialCapacity >= 0), "Illegal capacity value {" + initialCapacity + "}.");
    Preconditions.checkArgument((lf > 0 && !Float.isNaN(lf)), "Illegal load factor value {" + lf + "}");

    if (initialCapacity > MAXIMUM_CAPACITY) {
      initialCapacity = MAXIMUM_CAPACITY;
    }

    int capacity = 1;
    while (capacity < initialCapacity) {
      capacity <<= 1;
    }
    table = new Entry[capacity];
    loadFactor = lf;
    threshold = (int) (capacity * loadFactor);
  }

  /**
   * Create the instance from the given initial capacity and default load factor.
   *
   * @param initialCapacity the given initial capacity.
   * @exception IllegalArgumentException if the initial capacity is negative.
   */
  protected ReferenceHashMap(final int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  /**
   * Create the instance with default initial capacity (16) and default load factor.
   */
  protected ReferenceHashMap() {
    loadFactor = DEFAULT_LOAD_FACTOR;
    threshold = (int) (DEFAULT_INITIAL_CAPACITY);
    table = new Entry[DEFAULT_INITIAL_CAPACITY];
  }

  /**
   * Create the instance with the same mapping as the specified <tt>Map</tt>, the instance is
   * created with default load factor, which is <tt>0.75</tt> and an capacity sufficient to
   * hold the mappings in the specified <tt>Map</tt>.
   *
   * @param t the map whose mappings are to be placed in the map.
   * @exception NullPointerException if the specified map is {@code null}.
   */
  protected ReferenceHashMap(final Map<? extends K, ? extends V> t) {
    this(Math.max((int) (t.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY),
        DEFAULT_LOAD_FACTOR);
    putAllForCreate(t);
  }

  //---------------------------------------------------------------------------
  // helper functionality.
  //---------------------------------------------------------------------------
  /**
   * Value representing null keys inside tables.
   */
  private static final Object NULL_KEY = new Object();

  /**
   * Use NULL_KEY for key if it is null.
   *
   * @param key the given key to mark {@code null}.
   * @return the NULL_KEY if null or the key.
   */
  private static Object maskNull(final Object key) {
    return (key == null ? NULL_KEY : key);
  }

  /**
   * Return internal representation of null key back to caller as null.
   *
   * @param key the given key to un-mask {@code null}.
   * @return the {@code null} value if the key is null or the key value.
   */
  @SuppressWarnings({ "unchecked" })
  static <K> K unmaskNull(final Object key) {
    return (K) (key == NULL_KEY ? null : key);
  }

  /**
   * Whether to prefer the old supplemental hash function, for
   * compatibility with broken applications that rely on the
   * internal hashing order.
   *
   * Set to true only by hotspot when invoked via
   * -XX:+UseNewHashFunction or -XX:+AggressiveOpts
   */
  private static final boolean userNewHash;
  static { userNewHash = false; }

  private static int oldHash(int h) {
    h += ~(h << 9);
    h ^=  (h >>> 14);
    h +=  (h << 4);
    h ^=  (h >>> 10);
    return h;
  }

  private static int newHash(int h) {
    // This function ensures that hashCodes that differ only by
    // constant multiples at each bit position have a bounded
    // number of collisions (approximately 8 at default load factor).
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
  }

  /**
   * Applies a supplemental hash function to a given hashCode, which
   * defends against poor quality hash functions.  This is critical
   * because HashMap uses power-of-two length hash tables, that
   * otherwise encounter collisions for hashCodes that do not differ
   * in lower bits.
   */
  static int hash(int h) {
    return userNewHash ? newHash(h) : oldHash(h);
  }

  static int hash(Object key) {
    return hash(key.hashCode());
  }

  /**
   * Check for equality of non-null reference x and possibly-null y.  By
   * default uses Object.equals.
   */
  static boolean eq(final Object x, final Object y) {
    return x == y || x.equals(y);
  }

  /**
   * Return index for hash code h.
   */
  static int indexFor(final int h, final int length) {
    return h & (length - 1);
  }

  /**
   * Expunge stale entries from the table.
   */
  @SuppressWarnings({ "unchecked" })
  private void expungeStaleEntries() {
    Entry<K, V> entry;
    while ((entry = (Entry<K, V>) queue.poll()) != null) {
      int hash = entry.hash();
      int index = indexFor(hash, table.length);

      Entry<K, V> prev = table[index];
      Entry<K, V> p = prev;
      while (p != null) {
        Entry<K, V> next = p.next();
        if (p == entry) {
          if (prev == entry) {
            table[index] = next;
          } else {
            prev.setNext(next);
          }
          entry.setNext(null);
          entry.setValue(null);
          size--;
        }
        prev = p;
        p = next;
      }
    }
  }

  /**
   * Returns the table after first expunging stale entries.
   *
   * @return the table after first expunging stale entries.
   */
  @SuppressWarnings({ "unchecked" })
  private Entry[] getTable() {
    expungeStaleEntries();
    return table;
  }

  /**
   * Returns the number of key-value mapping in the map. This result is snapshot, and may not
   * reflect unprocessed entries that will be removed before next attempted access because they
   * are no longer referenced.
   */
  public int size() {
    if (size == 0) {
      return 0;
    }
    expungeStaleEntries();
    return size;
  }

  /**
   * Returns <tt>true</tt> if this map contains no key-value mappings, This result is snapshot,
   * and may not reflect unprocessed entries that will be removed before next attempted access
   * because thay are no longer referenced.
   */
  public boolean isEmpty() {
    return (size() == 0);
  }

  /**
   * Returns the value to which the specified key is mapped in this hash map, or {@code null} if
   * the map contains no mapping for this key. A return value of {@code null} does not necessarily
   * indicate that the map contains no mapping for the key; its also possible that the map
   * explicitly maps the key to {@code null}. The <tt>containsKey</tt> method may be used to
   * distinguish these two cases.
   *
   * @param   key the key whose associated value is to be returned.
   * @return  the value to which this map maps the specified key, or <tt>null</tt> if the map
   *          contains no mapping for this key.
   * @see ReferenceHashMap#put(Object, Object)
   */
  @SuppressWarnings({ "unchecked" })
  public V get(final Object key) {
    final Object k = maskNull(key);
    final int hash = hash(k);
    Entry[] t = getTable();
    int index = indexFor(hash, t.length);
    Entry<K, V> entry = t[index];
    while (entry != null) {
      if (entry.hash() == hash && eq(k, entry.get())) {
        return entry.getValue();
      }
      entry = entry.next();
    }

    return null;
  }

  /**
   * Returns <tt>true</tt> if this map contains a mapping for the specified key.
   * @param   key the key whose presence in this map is to be tested.
   * @return  is this map contains a mapping for the specified key.
   */
  public boolean containsKey(final Object key) {
    return (getEntry(key) != null);
  }

  /**
   * Returns the key associated with the specified key in the HashMap, return {@code null} if
   * HashMap contains no mapping for this key.
   */
  @SuppressWarnings({ "unchecked" })
  Entry<K, V> getEntry(final Object key) {
    final Object k = maskNull(key);
    final int hash = hash(k);
    Entry[] t = getTable();
    int index = indexFor(hash, t.length);
    Entry<K, V> entry = t[index];
    while (entry != null && !(entry.hash() == hash && eq(k, entry.get()))) {
      entry = entry.next();
    }
    return entry;
  }

  /**
   * Associates the specified value with the specified key in this map. If the map previously
   * contained a mapping for this key, the old value is replaced.
   *
   * @param key   key with which the specified value is to be associated.
   * @param value value to be associated with the specified key.
   * @return previous value associated with specified key, or {@code null} if there was no mapping
   *         for key. A {@code null} return can also indicate that the HashMap previously
   *         associated {@code null} with the specified key.
   */
  @SuppressWarnings({ "unchecked" })
  public V put(final K key, final V value) {
    final K k = (K) maskNull(key);
    final int hash = hash(k);
    Entry[] t = getTable();
    final int index = indexFor(hash, t.length);

    for (Entry<K, V> entry = t[index]; entry != null; entry = entry.next()) {
      if (hash == entry.hash() && eq(k, entry.get())) {
        V ov = entry.getValue();
        if (value != ov) {
          entry.setValue(value);
        }
        return ov;
      }
    }

    modCount++;
    Entry<K, V> entry = t[index];
    t[index] = createEntry(k, value, queue, hash, entry);
    if (++size >= threshold) {
      resize(t.length * 2);
    }
    return null;
  }

  /**
   * This method is used instead of <tt>put</tt> by constructor and pseudo-constructors
   * (clone, readObject). Its does not resize the table, check for modification.
   */
  @SuppressWarnings({ "unchecked" })
  private void putForCreate(final K key, final V value) {
    final K k = (K) maskNull(key);
    final int hash = hash(k);
    final int index = indexFor(hash, table.length);

    /*
     * Look for pre-existing entry for key. This will never happen for clone clone and deserialize.
     */
    for (Entry<K, V> entry = table[index]; entry != null; entry = entry.next()) {
      if (hash == entry.hash() && eq(k, entry.get())) {
        entry.setValue(value);
        return;
      }
    }

    modCount++;
    Entry<K, V> entry = table[index];
    table[index] = createEntry(k, value, queue, hash, entry);
  }

  /**
   * This method is used instead of <tt>putAll</tt> by constructor and pseudo-constructors(clone).
   * Its does not resize the table, check for modification.
   */
  void putAllForCreate(final Map<? extends K, ? extends V> m) {
    for (final Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator();
      i.hasNext();) {
      final Map.Entry<? extends K, ? extends V> entry = i.next();
      putForCreate(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Rehashes the contents of this maps in to a new array with a large capacity. This method is
   * called automatically when the number of keys in this map is reaches its threshold.
   *
   * If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets
   * threshold to Integer.MAX_VALUE. This has the effect of preventing future call.
   *
   * @param newCapacity the new capacity, MUST be a power of two; must greater than the current
   *        capacity unless current capacity is MAXIMUM_CAPACITY (in which case value is
   *        irrelevant).
   */
  @SuppressWarnings({ "unchecked" })
  void resize(final int newCapacity) {
    Entry[] oldTable = getTable();
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
      threshold = Integer.MAX_VALUE;
      return;
    }

    Entry[] newTable = new Entry[newCapacity];
    transfer(oldTable, newTable);
    table = newTable;

    /*
     * If ignoring null elements and processing ref queue caused massive shrinkage, then restore
     * old table. This should be rare, but avoids unbounded expansion of garbage-filled table.
     */
    if (size >= threshold / 2) {
      threshold = (int) (newCapacity * loadFactor);
    } else {
      expungeStaleEntries();
      transfer(newTable, oldTable);
      table = oldTable;
    }
  }

  /**
   * Transfer all entries from source to destination table.
   */
  @SuppressWarnings({ "unchecked" })
  private void transfer(final Entry[] src, final Entry[] dest) {
    for (int index = 0; index < src.length; index++) {
      Entry<K, V> entry = src[index];
      src[index] = null;
      while (entry != null) {
        Entry<K, V> next = entry.next();
        Object key = entry.get();
        if (key == null) {
          entry.setNext(null);
          entry.setValue(null);
          size--;
        } else {
          int i = indexFor(entry.hash(), dest.length);
          entry.setNext(dest[i]);
          dest[i] = entry;
        }
        entry = next;
      }
    }
  }

  /**
   * Copies all of the mappings from the specified map to this map. These mappings will replace any
   * mappings that this map had for any of the keys currently in the specified map.
   *
   * @param t mappings to be stored in this map.
   */
  public void putAll(Map<? extends K, ? extends V> t) {
    if (t == null || t.isEmpty()) {
      return;
    }

    int numKeysToBeAdded = t.size();
    /*
     * Expand the map if the map if the number of mappings to be added is greater than or equals
     * to threshold. This is conservative; the obvious condition is (m.size + size() >= threshold),
     * but this condition could result in a map with twice the appropriate capacity, if the key
     * to be added overlap with the keys already in the map. By using the conservative calculation,
     * we subject ourself to at most one extra resize.
     */
    if (numKeysToBeAdded > threshold) {
      int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
      if (targetCapacity > MAXIMUM_CAPACITY) {
        targetCapacity = MAXIMUM_CAPACITY;
      }
      int newCapacity = table.length;
      while (newCapacity < targetCapacity) {
        newCapacity <<= 1;
      }
      if (newCapacity > table.length) {
        resize(newCapacity);
      }
    }

    for (final Iterator<? extends Map.Entry<? extends K, ? extends V>> i = t.entrySet().iterator();
      i.hasNext();) {
      final Map.Entry<? extends K, ? extends V> entry = i.next();
      put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Removes the mapping for this key from this map if present.
   *
   * @param key key whose mapping is to be removed from the map.
   * @return previous value associated with the specified key, or {@code null} if there was no
   *         mapping for this key. A {@code null} return can also indicate that the map previously
   *         associated {@code null} with the specified key.
   */
  @SuppressWarnings({ "unchecked" })
  public V remove(final Object key) {
    final Object k = maskNull(key);
    final int hash = hash(k);
    final Entry[] t = getTable();
    final int index = indexFor(hash, t.length);
    Entry<K, V> prev = t[index];
    Entry<K, V> entry = prev;

    while (entry != null) {
      final Entry<K, V> next = entry.next();
      if (hash == entry.hash() && eq(k, entry.get())) {
        modCount++;
        size--;
        if (prev == entry) {
          t[index] = next;
        } else {
          prev.setNext(next);
        }
        return entry.getValue();
      }
      prev = entry;
      entry = next;
    }

    return null;
  }

  /** Special version of remove needed by entry set. */
  @SuppressWarnings({ "unchecked" })
  Entry<K, V> removeMapping(final Object o) {
    if (!(o instanceof Map.Entry)) {
      return null;
    }

    final Entry[] t = getTable();
    final Map.Entry entry = (Map.Entry) o;
    final Object key = maskNull(entry.getKey());
    final int hash = hash(key);
    final int index = indexFor(hash, t.length);
    Entry<K, V> prev = t[index];
    Entry<K, V> e = prev;

    while (e != null) {
      final Entry<K, V> next = e.next();
      if (hash == e.hash() && e.equals(entry)) {
        modCount++;
        size--;
        if (prev == e) {
          t[index] = next;
        } else {
          prev.setNext(next);
        }
        return e;
      }
      prev = e;
      e = next;
    }

    return null;
  }

  /**
   * Removes all mappings from this map.
   */
  @Override
  @SuppressWarnings({ "unchecked" })
  public void clear() {
    // clear reference queue. We don't need to expunge entries since table is getting cleared.
    while (queue.poll() != null) {
      ;
    }

    modCount++;
    final Entry[] t = table;
    for (int index = 0; index < t.length; index++) {
      t[index] = null;
    }
    size = 0;

    // Allocation of array may have caused GC, which may have caused additional entries to go
    // stale. Removing these entries from the reference queue will make them eligible for
    // reclamation.
    while (queue.poll() != null) {
      ;
    }
  }

  /**
   * Returns {@code true} if this map maps one or more keys to the specified value.
   *
   * @param value the given value whose presence in this map is to be tested.
   * @return if this map maps one or more keys to the specified value.
   */
  @Override
  @SuppressWarnings({ "unchecked" })
  public boolean containsValue(final Object value) {
    if (value == null) {
      return containsNullValue();
    }

    final Entry[] t = getTable();
    for (int index = 0; index < t.length; index++) {
      for (Entry<K, V> entry = t[index]; entry != null; entry = entry.next()) {
        if (value.equals(entry.getValue())) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Special-case code for containsValue with null argument.
   */
  @SuppressWarnings({ "unchecked" })
  private boolean containsNullValue() {
    final Entry[] t = getTable();
    for (int index = 0; index < t.length; index++) {
      for (Entry<K, V> entry = t[index]; entry != null; entry = entry.next()) {
        if (entry.getValue() == null) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns a shallow copy of this <tt>ReferenceHashMap</tt> instance: the keys and values
   * themselves are not cloned.
   *
   * @return a shallow copy.
   */
  @Override
  @SuppressWarnings({ "unchecked" })
  public Object clone() throws CloneNotSupportedException {
    ReferenceHashMap<K, V> result = null;
    try {
      result = (ReferenceHashMap<K, V>) super.clone();
    } catch (final CloneNotSupportedException cnsex) {
      ;
    }

    result.table = new Entry[table.length];
    result.entrySet = null;
    result.modCount = 0;
    result.size = 0;
    result.putAllForCreate(this);

    return result;
  }

  /**
   * Entry.
   *
   * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
   * @version $Id: ReferenceHashMap.java 2009-07-29 17:30:16z nguyen_dv $
   *
   * Create date: Jul 29, 2009
   * <pre>
   *  Initialization Entry class.
   * </pre>
   */
  interface Entry<K, V> extends Map.Entry<K, V> {
    /**
     * Returns the reference value.
     *
     * @return the reference value.
     */
    K get();

    /**
     * Returns the next entry.
     *
     * @return the next entry.
     */
    Entry<K, V> next();

    /**
     * Set next entry value.
     *
     * @param next the given next {@link Entry entry} to set.
     */
    void setNext(Entry<K, V> next);

    /**
     * Returns the hash value.
     *
     * @return the hash value.
     */
    int hash();
  }

  /**
   * HashIterator.
   *
   * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
   * @version $Id: ReferenceHashMap.java 2009-07-29 16:44:16z nguyen_dv $
   *
   * Create date: Jul 29, 2009
   * <pre>
   *  Initialization HashIterator class.
   * </pre>
   */
  private abstract class HashIterator<T> implements Iterator<T> {
    private int index;
    private Entry<K, V> entry = null;
    private Entry<K, V> lastReturned = null;
    private int expectedModCount = modCount;

    /**
     * Strong reference needed to avoid disappearance of key between hasNext and next.
     */
    private Object nextKey = null;
    /**
     * Strong reference needed to avoid disappearance of key between nextEntry and any use of
     * the entry.
     */
    private Object currentKey = null;

    /**
     * Create <tt>HashIterator</tt> instance with default constructor.
     */
    HashIterator() {
      index = (size() != 0 ? table.length : 0);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @SuppressWarnings({ "unchecked" })
    public boolean hasNext() {
      final Entry[] t = table;

      while (nextKey == null) {
        Entry<K, V> e = entry;
        int i = index;
        while (e == null && i > 0) {
          e = t[--i];
        }
        entry = e;
        index = i;
        if (e == null) {
          currentKey = null;
          return false;
        }
        nextKey = entry.get(); // hold the key in strong reference.
        if (nextKey == null) {
          entry = entry.next();
        }
      }

      return true;
    }

    /**
     * Returns the next entry reference.
     *
     * @return the next entry reference.
     */
    protected Entry<K, V> nextEntry() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if (nextKey == null && !hasNext()) {
        throw new NoSuchElementException();
      }

      lastReturned = entry;
      entry = entry.next();
      currentKey = nextKey;
      nextKey = null;
      return lastReturned;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      if (lastReturned == null) {
        throw new java.lang.IllegalStateException();
      }
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }

      ReferenceHashMap.this.remove(currentKey);
      expectedModCount = modCount;
      lastReturned = null;
      currentKey = null;
    }
  }

  /**
   * ValueIterator.
   *
   * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
   * @version $Id: ReferenceHashMap.java 2009-07-29 17:46:40z nguyen_dv $
   *
   * Create date: Jul 29, 2009
   * <pre>
   *  Initialization ValueIterator class.
   * </pre>
   */
  private class ValueIterator extends HashIterator<V> {
    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    public V next() {
      return nextEntry().getValue();
    }
  }

  /**
   * KeyIterator.
   *
   * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
   * @version $Id: ReferenceHashMap.java 2009-07-29 17:48:27z nguyen_dv $
   *
   * Create date: Jul 29, 2009
   * <pre>
   *  Initialization KeyIterator class.
   * </pre>
   */
  private class KeyIterator extends HashIterator<K> {
    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    public K next() {
      return nextEntry().getKey();
    }
  }

  /**
   * EntryIterator.
   *
   * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
   * @version $Id: ReferenceHashMap.java 2009-07-29 17:50:52z nguyen_dv $
   *
   * Create date: Jul 29, 2009
   * <pre>
   *  Initialization EntryIterator class.
   * </pre>
   */
  private class EntryIterator extends HashIterator<Map.Entry<K, V>> {
    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    public Map.Entry<K, V> next() {
      return nextEntry();
    }
  }

  // Views
  private transient volatile Set<K>        keySet = null;
  private transient volatile Collection<V> values = null;
  private transient Set<Map.Entry<K, V>>   entrySet = null;
  /**
   * Returns a set view of the keys contained in the map. This set is backed by the map, so changes
   * to the map are reflected in the set, and vice-versa. The set support element removal, which
   * removes the corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
   * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations. Its
   * does not support <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * returns a set view of the keys contained in the map.
   */
  @Override
  public Set<K> keySet() {
    final Set<K> ks = keySet;
    return (ks != null ? ks : (keySet = new KeySet()));
  }

  /**
   * KeySet.
   *
   * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
   * @version $Id: ReferenceHashMap.java 2009-07-29 18:10:44z nguyen_dv $
   *
   * Create date: Jul 29, 2009
   * <pre>
   *  Initialization KeySet class.
   * </pre>
   */
  private class KeySet extends AbstractSet<K> {
    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<K> iterator() {
      return new KeyIterator();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
      return ReferenceHashMap.this.size();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(final Object o) {
      return containsKey(o);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(final Object o) {
      if (containsKey(o)) {
        ReferenceHashMap.this.remove(o);
        return true;
      } else {
        return false;
      }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
      ReferenceHashMap.this.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#toArray()
     */
    @Override
    public Object[] toArray() {
      final Collection<K> keys = new ArrayList<K>(size());
      for (final Iterator<K> iterator = iterator(); iterator.hasNext();) {
        keys.add(iterator.next());
      }

      return keys.toArray();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#toArray(T[])
     */
    public <T> T[] toArray(final T[] a) {
      final Collection<K> keys = new ArrayList<K>(size());
      for (final Iterator<K> iterator = iterator(); iterator.hasNext();) {
        keys.add(iterator.next());
      }

      return keys.toArray(a);
    }
  }

  /**
   * Returns a collection view of the values contained in the map. The collection is backed in the
   * map, so change to the map are reflected in the collection, and vice-versa. The collection
   * supports element removal, which removes the corresponding mapping from this map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>,
   * and <tt>clear</tt> operators. Its does not support the <tt>add</tt> or <tt>addAll</tt>
   * operators.
   *
   * @return a collection view of the values contained in th map.
   */
  @Override
  public Collection<V> values() {
    final Collection<V> vs = values;
    return (vs != null ? vs : (values = new Values()));
  }

  /**
   * Values.
   *
   * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
   * @version $Id: ReferenceHashMap.java 2009-07-29 18:35:14z nguyen_dv $
   *
   * Create date: Jul 29, 2009
   * <pre>
   *  Initialization Values class.
   * </pre>
   */
  private class Values extends AbstractCollection<V> {
    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<V> iterator() {
      return new ValueIterator();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
      return ReferenceHashMap.this.size();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(final Object o) {
      return ReferenceHashMap.this.containsValue(o);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
      ReferenceHashMap.this.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#toArray()
     */
    @Override
    public Object[] toArray() {
      final Collection<V> vs = new ArrayList<V>(size());
      for (final Iterator<V> iterator = iterator(); iterator.hasNext();) {
        vs.add(iterator.next());
      }

      return vs.toArray();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#toArray(T[])
     */
    public <T> T[] toArray(final T[] a) {
      final Collection<V> vs = new ArrayList<V>(size());
      for (final Iterator<V> iterator = iterator(); iterator.hasNext();) {
        vs.add(iterator.next());
      }

      return vs.toArray(a);
    }
  }

  /**
   * Returns a set of view of the mappings contained in the map. Each element in the returned
   * set is a <tt>Map.Entry</tt>. The set is backed by the map, so changes to the map are
   * reflected in the set, and vice-versa. The set supports element removal, which remove the
   * corresponding mapping from the map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support
   * the <tt>add</tt>, or <tt>addAll</tt> operations.
   *
   * @return a set of view of the mappings contained in the map.
   */
  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    final Set<Map.Entry<K, V>> es = entrySet;
    return (es != null ? es : (entrySet = new EntrySet()));
  }
  /**
   * EntrySet.
   *
   * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
   * @version $Id: ReferenceHashMap.java 2009-07-29 18:53:08z nguyen_dv $
   *
   * Create date: Jul 29, 2009
   * <pre>
   *  Initialization EntrySet class.
   * </pre>
   */
  private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public boolean contains(final Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }

      final Map.Entry entry = (Map.Entry) o;
      final Entry candiate = getEntry(entry.getKey());

      return candiate != null && candiate.equals(entry);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(final Object o) {
      return (removeMapping(o) != null);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
      return ReferenceHashMap.this.size();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
      ReferenceHashMap.this.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#toArray()
     */
    @Override
    public Object[] toArray() {
      final Collection<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(size());
      for (final Iterator<Map.Entry<K, V>> iterator = iterator(); iterator.hasNext();) {
        entries.add(iterator.next());
      }

      return entries.toArray();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.AbstractCollection#toArray(T[])
     */
    public <T> T[] toArray(final T[] a) {
      final Collection<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(size());
      for (final Iterator<Map.Entry<K, V>> iterator = iterator(); iterator.hasNext();) {
        entries.add(iterator.next());
      }

      return entries.toArray(a);
    }
  }

  /**
   * Save state of the <tt>ReferenceHashMap</tt> instance to a stream (i.e, serialize it).
   *
   * @param stream the given stream to write object.
   */
  private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
    // get entry set.
    final Iterator<Map.Entry<K, V>> iterator = entrySet().iterator();

    // write out the threshold, load factor, and any hidden stuff.
    stream.defaultWriteObject();

    // write out the number of buckets
    stream.writeInt(table.length);

    // write out size (number of mapping)
    stream.writeInt(size);

    // write out the key and value (alternating).
    while (iterator.hasNext()) {
      final Map.Entry<K, V> entry = iterator.next();
      stream.writeObject(entry.getKey());
      stream.writeObject(entry.getValue());
    }
  }

  /**
   * Reconstitutes the <tt>ReferenceHashMap</tt> instance from stream (i.e, deserialize it).
   *
   * @param stream the given stream to read object.
   */
  @SuppressWarnings({ "unchecked" })
  private void readObject(final java.io.ObjectInputStream stream) throws IOException,
    ClassNotFoundException {
    // read the threshold, load factor, and any hidden stuff.
    stream.defaultReadObject();

    // create reference queue.
    queue = new ReferenceQueue<K>();

    // read the number of buckets and allocated the buckets array.
    int capacity = stream.readInt();
    table = new Entry[capacity];

    // read the size (number of mapping)
    int s = stream.readInt();
    for (int index = 0; index < s; index++) {
      K key = (K) stream.readObject();
      V value = (V) stream.readObject();
      putForCreate(key, value);
    }
  }

  // These methods are used when serializing HashSets.
  int capacity() { 
    return table.length; 
  }

  float loadFactor() {
    return loadFactor;
  }
  

  /**
   * Returns the {@link com.inet.base.ref.ReferenceHashMap.Entry entry} instance from the given
   * key, value, queue reference, hash value and the next entry.
   *
   * @param key   the given key instance.
   * @param value the given value instance.
   * @param q     the given {@link ReferenceQueue reference queue} instance.
   * @param hash  the given hash value.
   * @param next  the next entry value.
   * @return the entry instance.
   */
  protected abstract Entry<K, V> createEntry(K key, V value, ReferenceQueue<K> q,
      int hash, Entry<K, V> next);
}
