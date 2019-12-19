package almazon_PC;

public class Personal {
	Almazon_PC almazon; 
	
	static final int NUM_CLIENTES = 4; 
	static final int NUM_ADMIN = 2; 
	static final int NUM_ENCARGADO = 1;

	private void hiloCliente() {
		try {
			Thread.sleep(500);
			almazon.Cliente();
			
		} catch (InterruptedException e) {
			
		}
	}
	private void hiloAdmin() {
		try {
			almazon.EmpleadoAdministrativo();
			
		} catch (InterruptedException e) {
			
		}
	}
	
	/*private void hiloEncargado() {
		try {
			almazon.EmpleadoEncargado();
			
		} catch (InterruptedException e) {
			
		}
	}*/
	
	public void exec() {
		almazon = new Almazon_PC(); 
		for (int i = 0; i < NUM_CLIENTES; i++) {
			new Thread(() -> hiloCliente()).start();
		}
		for (int i = 0; i < NUM_ADMIN; i++) {
			new Thread(() -> hiloAdmin()).start();
		}
		/*for (int i = 0; i < NUM_ENCARGADO; i++) {
			new Thread(() -> hiloEncargado()).start();
		}*/
		
	}
	
	public static void main(String[] args) {
		new Personal().exec();
	}
}
