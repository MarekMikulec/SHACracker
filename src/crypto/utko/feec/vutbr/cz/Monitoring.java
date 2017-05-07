package crypto.utko.feec.vutbr.cz;

public class Monitoring extends Thread{

	private boolean stop  = false;
	
	
	@Override
	public void run() {
		for(;;){
			Runtime rt = Runtime.getRuntime();
			System.out.println("Alocated memory: " + (rt.totalMemory()/1024)/1024 + "MB, max memory: " + ((rt.maxMemory()/1024)/1024)/1024+"GB");
			if(stop){
				break;
			}
			try {
				Monitoring.sleep(1000000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	
}
