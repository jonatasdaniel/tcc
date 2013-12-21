package br.furb.rma;


public class Util {

	private static long inicio;
	
	public static long usedMemory() {
		long freeSize = 0L;
		long totalSize = 0L;
		long usedSize = -1L;
		try {
			Runtime info = Runtime.getRuntime();
			freeSize = info.freeMemory();
			totalSize = totalMemory();
			usedSize = totalSize - freeSize;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return usedSize;
	}

	public static long totalMemory() {
		long totalSize = 0L;
		try {
			Runtime info = Runtime.getRuntime();

			totalSize = info.totalMemory();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalSize;
	}

	public static long usedInPercent(long usedMemory) {
		long total = Util.totalMemory();
		long percent = usedMemory * 100 / total;
		return percent;
	}

	public static void iniciar() {
		inicio = System.currentTimeMillis();
	}

	public static long fim() {
		return System.currentTimeMillis() - inicio;
	}

}
