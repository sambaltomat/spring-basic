package id.co.sambaltomat.core.dao;

import org.hibernate.criterion.Order;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: 30-Apr-2009
 * Time: 08:44:30
 * To change this template use File | Settings | File Templates.
 */
public class OrderEntry {
	private String propertyName;
	private Order order;

	public OrderEntry(String propertyName, Order order) {
		this.propertyName = propertyName;
		this.order = order;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((order == null) ? 0 : order.hashCode());
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
		final OrderEntry other = (OrderEntry) obj;
		if (order == null) {
			if (other.order != null)
				return false;
		} else if (!order.equals(other.order))
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		} else if (!propertyName.equals(other.propertyName))
			return false;
		return true;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}

}
