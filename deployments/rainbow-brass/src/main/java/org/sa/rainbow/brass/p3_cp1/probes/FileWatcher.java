package org.sa.rainbow.brass.p3_cp1.probes;

import java.nio.*;
import java.nio.file.*;
import java.io.*;


interface IFileWatcherCaller{
	void hasChanged();
}


public class FileWatcher extends Thread {
	private boolean m_debug = false;
	private Path m_filePath;
	private WatchService m_watchService;
	private IFileWatcherCaller m_caller;

	public FileWatcher (String target, IFileWatcherCaller callback){
		m_filePath = Paths.get(target);
		m_caller = callback;
		try {
			m_watchService = FileSystems.getDefault().newWatchService();

			//listen for create ,delete and modify event kinds
			m_filePath.register(m_watchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	
	public void run(){
		watch();
	}
	
	public void watch() {

		if (m_debug)
			System.out.println("running...");

		while (true) {
			WatchKey key;
			try {
				//return signaled key, meaning events occurred on the object
				key = m_watchService.take();
			} catch (InterruptedException ex) {
				return;
			}

			//retrieve all the accumulated events
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();               
				
				if (m_debug){
					System.out.println("kind "+ kind.name());
					Path path = (Path)event.context();
					System.out.println(path.toString());
				}
				
				if(kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
					if (m_debug)
						System.out.println("File changed.");
					m_caller.hasChanged();
				}
			}             
			//resetting the key goes back ready state
			key.reset();
			}
	}

}

