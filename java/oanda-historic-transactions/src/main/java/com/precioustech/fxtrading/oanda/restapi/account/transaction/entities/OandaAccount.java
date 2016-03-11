package com.precioustech.fxtrading.oanda.restapi.account.transaction.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "oanda_account")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "accounts")
public class OandaAccount implements Serializable {

	private static final long serialVersionUID = 7551620715795946711L;
	@Id
	@Column(name = "account_id")
	private Long accountId;
	@Column(name = "currency", nullable = false, unique = true)
	private String accountCurrency;

	public OandaAccount() {

	}

	public OandaAccount(Long accountId, String accountCurrency) {
		this.accountCurrency = accountCurrency;
		this.accountId = accountId;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getAccountCurrency() {
		return accountCurrency;
	}

	public void setAccountCurrency(String accountCurrency) {
		this.accountCurrency = accountCurrency;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountCurrency == null) ? 0 : accountCurrency.hashCode());
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
		OandaAccount other = (OandaAccount) obj;
		if (accountCurrency == null) {
			if (other.accountCurrency != null)
				return false;
		} else if (!accountCurrency.equals(other.accountCurrency))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%d - %s", this.accountId, this.accountCurrency);
	}
}
