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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;

/**
 * SoftHashMap.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: SoftHashMap.java 2009-07-30 02:45:40z nguyen_dv $
 *
 * @since 1.0
 */
public final class SoftHashMap<K, V> extends ReferenceHashMap<K, V> {
  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = 8868709603253695675L;

  /**
   * Create <tt>SoftHashMap</tt> instance with default capacity (16) and default load factor.
   */
  public SoftHashMap() {
    super();
  }

  /**
   * Create <tt>SoftHashMap</tt> instance from the given initial capacity and default load factor.
   *
   * @param capacity the given initial capacity.
   * @exception IllegalArgumentException if the given capacity is negative.
   */
  public SoftHashMap(int capacity) {
    super(capacity);
  }

  /**
   * Create <tt>SoftHashMap</tt> instance from the given initial capacity and load factor value.
   *
   * @param capacity   the given initial capacity.
   * @param loadFactor the load factor of the hash map.
   * @exception IllegalArgumentException if the initial capacity is negative, or if the load factor
   *            is non-positive.
   */
  public SoftHashMap(int capacity, float loadFactor) {
    super(capacity, loadFactor);
  }

  /**
   * Create <tt>SoftHashMap</tt> instance with the same mapping as the specified <tt>Map</tt>, the
   * instance is created with default load factor, which is <tt>0.75</tt> and an capacity
   * sufficient to hold the mappings in the specified <tt>Map</tt>.
   *
   * @param m the map whose mappings are to be placed in the map.
   * @exception NullPointerException if the specified map is {@code null}.
   */
  public SoftHashMap(final Map<? extends K, ? extends V> m) {
    super(m);
  }

  /**
   * {@inheritDoc}
   *
   * @see com.inet.base.ref.ReferenceHashMap#createEntry(java.lang.Object, java.lang.Object,
   * java.lang.ref.ReferenceQueue, int, com.inet.base.ref.ReferenceHashMap.Entry)
   */
  @Override
  protected ReferenceHashMap.Entry<K, V> createEntry(final K key, final V value,
      final ReferenceQueue<K> queue, final int hash, final ReferenceHashMap.Entry<K, V> next) {
    return new SoftEntry<K, V>(key, value, queue, hash, next);
  }

  /**
   * SoftEntry.
   *
   * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
   * @version $Id: SoftHashMap.java 2009-07-30 02:47:07z nguyen_dv $
   *
   * Create date: Jul 30, 2009
   * <pre>
   *  Initialization WeakEntry class.
   * </pre>
   */
  private static class SoftEntry<K, V> extends SoftReference<K>
    implements ReferenceHashMap.Entry<K, V> {
    private V value;
    private final int hash;
    private Entry<K, V> next;

    /**
     * Create <tt>SoftEntry</tt> instance.
     */
    SoftEntry(final K key, final V v, final ReferenceQueue<K> queue,
        final int h, final Entry<K, V> n) {
      super(key, queue);
      this.value = v;
      this.hash = h;
      this.next = n;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map.Entry#getKey()
     */
    public K getKey() {
      return SoftHashMap.<K>unmaskNull(get());
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map.Entry#getValue()
     */
    public V getValue() {
      return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.Map.Entry#setValue(java.lang.Object)
     */
    public V setValue(final V newValue) {
      final V oldValue = value;
      value = newValue;
      return oldValue;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.inet.base.ref.ReferenceHashMap.Entry#hash()
     */
    public int hash() {
      return hash;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.inet.base.ref.ReferenceHashMap.Entry#next()
     */
    public ReferenceHashMap.Entry<K, V> next() {
      return next;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.inet.base.ref.ReferenceHashMap.Entry#setNext(
     * com.inet.base.ref.ReferenceHashMap.Entry)
     */
    public void setNext(final ReferenceHashMap.Entry<K, V> n) {
      this.next = n;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public boolean equals(final Object obj) {
      if (!(obj instanceof Map.Entry)) {
        return false;
      }
      final Map.Entry entry = (Map.Entry) obj;

      final Object k1 = getKey();
      final Object k2 = entry.getKey();
      if (k1 == k2 || (k1 != null && k1.equals(k2))) {
        final Object v1 = getValue();
        final Object v2 = entry.getValue();
        return (v1 == v2 || (v1 != null && v1.equals(v2)));
      }

      return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      Object k = getKey();
      Object v = getValue();

      return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return getKey() + "=" + getValue();
    }
  }
}
