package br.furb.rma;

public class LUTable {

	private int[] values;
	private double a = 1;
	private double b = 127;

	public LUTable() {
		values = new int[256];
		init();
	}

	public int getValue(int x) {
		return values[x];
	}

	public void setContrast(double a) {
		this.a = a;
		init();
	}

	public void setBrightness(double b) {
		this.b = b;
		init();
	}

	private void init() {
		for (int x = 0; x <= 255; x++) {
			values[x] = (int) (a * (x - b) + 127.);

			if (values[x] > 255) {
				values[x] = 255;
			}

			if (values[x] < 0) {
				values[x] = 0;
			}
		}
	}

}