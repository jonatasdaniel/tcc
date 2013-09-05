package br.furb.rma.models;

import java.io.Serializable;

public class DicomPatient implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String gender;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

}