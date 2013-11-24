package id.co.sambaltomat.core.dao;

import org.hibernate.criterion.Criterion;

/**
 * Class yang digunakan untuk membungkus criterion yang merupakan property ke tabel lain
 * name diisi dengan nama property
 * misal : jika ingin membuat criteria untuk license.identity, maka "name" diisi dengan identity
 * kalau criterion bukan ke tabel lain, maka name diisi null
 *
 * @author peter
 *
 */
public class CriterionEntry {
	private String propertyName;
	private Criterion criterion;

	public CriterionEntry(String propertyName, Criterion criterion) {
		this.propertyName = propertyName;
		this.criterion = criterion;
	}

	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public Criterion getCriterion() {
		return criterion;
	}
	public void setCriterion(Criterion criterion) {
		this.criterion = criterion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((criterion == null) ? 0 : criterion.hashCode());
		result = prime * result
				+ ((propertyName == null) ? 0 : propertyName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final CriterionEntry other = (CriterionEntry) obj;
		if (criterion == null) {
			if (other.criterion != null)
				return false;
		} else if (!criterion.equals(other.criterion))
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		return true;
	}
}
