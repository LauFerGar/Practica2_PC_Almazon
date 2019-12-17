package almazon_PC;

public class Personal {
	Almazon almazon; 
	
	static final int NUM_EMPLEADOS = 2; 

	private void hiloCliente(int nombre_cliente) {
		try {
			Thread.sleep(500);
			almazon.Cliente(nombre_cliente);
		} catch (InterruptedException e) {}
	}
	private void hiloAdmin() {
		try {
			almazon.EmpleadoAdministrativo();
		} catch (InterruptedException e) {}
	}
	
	public void exec() {
		almazon = new Almazon(); 
		for (int i = 0; i < NUM_EMPLEADOS; i++) {
			int numCliente = i;
			new Thread(() -> hiloCliente(numCliente)).start();
			new Thread(() -> hiloAdmin()).start();
		}
	}
	
	public static void main(String[] args) {
		new Personal().exec();
	}
}
