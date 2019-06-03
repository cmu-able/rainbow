package org.sa.rainbow.brass.p3_cp1.probes;


public class FileWatcherTest implements IFileWatcherCaller{
		public void hasChanged(){
			System.out.println("It has changed!");
		}
		
		public static void main (String[] args) throws Exception {
			IFileWatcherCaller caller = new FileWatcherTest();
			FileWatcher fw = new FileWatcher("/path/to/folder/", caller);
			fw.start();
				
			while (true){
			}
		}
}
