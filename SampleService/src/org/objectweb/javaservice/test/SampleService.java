/*
 * (c) Copyright 2004 Multiplan Consultants Ltd.
 * 
 * Project : SampleService
 * File    : SampleService.java
 * Created : 09-Nov-2004
 * Author  : John Rutter
 *
 */
package org.objectweb.javaservice.test;

/**
 * This class is provided as an example of how a Java application may be
 * run as a Windows System Service (aka daemon, in Unix-speak) using the
 * JavaService software utility.
 */
public class SampleService
{

	/**
	 * Default entry point if class is run as a standard application.
	 * This outputs a brief informational message to track use of the function.
	 * If command-line parameter specified as 'start' or 'stop', then it calls
	 * the service start or stop function respectively. 
	 * 
	 * @param args command-line arguments, array may be zero-length or more
	 */
	public static void main(String[] args)
	{
		System.out.println("SampleService application main entry point invoked...");
		
		if (args.length == 0)
		{
			System.out.println("Doing nothing, no command-line parameter(s) specified");
		}
		else if ("start".equalsIgnoreCase(args[0]))
		{
			System.out.println("Start parameter specified");
			serviceStart(args);
		}
		else if ("stop".equalsIgnoreCase(args[0]))
		{
			System.out.println("Stop parameter specified");
			serviceStop(args);
		}
		else
		{
			System.out.println("Command-line parameter '" + args[0] + "' not recognised, doing nothing");
		}
	}

	/**
	 * Convenience function to display details of the JVM in use.
	 */	
	public static void outputJvmDetails()
	{
		System.out.println("JVM " + 
							System.getProperty("java.vm.vendor", "{vm.vendor}")
							+ " " +
							System.getProperty("java.vm.name", "{vm.name}")
							+ " " +
							System.getProperty("java.vm.version", "{vm version}"));
	}

	/**
	 * Convenience function to display details of the current heap size.
	 */	
	public static void outputHeapDetails()
	{
		Runtime rt = Runtime.getRuntime();
		final long totalBytes = rt.totalMemory();
		final long freeBytes = rt.freeMemory();
		rt = null;

		System.out.println("Heap Size " +
							kBytes(totalBytes)
							+ " total, free "
							+ kBytes(freeBytes)
							+ " (used "
							+ kBytes(totalBytes - freeBytes)
							+ ")");    
	}

	/**
	 * Generate printable string for kilobytes, from number of bytes supplied.
	 * 
	 * @param bytes number of bytes to be converted into kb
	 * @return string representation of whole number of KB, with rounding
	 */
	private static String kBytes(long bytes)
	{
		if (bytes > 1024)
		{
			bytes -= (bytes % 1024); // rounding to get whole KB figures
		}

		final long kb = (long) bytes / 1024;

		return Long.toString(kb) + "KB";
	}


	/**
	 * Entry point for class to be loaded and started as a service.
	 * When run, this creates the singleton class instance and invokes the
	 * processing function within that - which continues in a loop (with delays)
	 * until signalled to end by the service stop function 
	 * 
	 * @param args command-line arguments, array may be zero-length or more
	 */
	public static void serviceStart(String[] args)
	{
		System.out.println("Service start function invoked...");

		outputJvmDetails();
		outputHeapDetails();

		getServiceInstance().execute();
	}

	/**
	 * Entry point for class to be be stopped when running as a service.
	 * Get singleton instance and signal that it is to terminate the processing
	 * loop (if that is even running at the time).
	 * 
	 * @param args command-line arguments, array may be zero-length or more
	 */
	public static void serviceStop(String[] args)
	{
		System.out.println("Service stop function invoked...");

		getServiceInstance().abort();

		outputHeapDetails();
	}

	
	/** Singleton service instance, lazy-loading creation */
	private static SampleService serviceInstance = null;

	/** Accessor to get/create the singleton instance when required */	
	private static synchronized SampleService getServiceInstance()
	{
		if (serviceInstance == null)
		{
			serviceInstance = new SampleService();
		}
		return serviceInstance;
	}


	/** Control flag, set/cleared/checked with synchronized functions */	
	private boolean serviceExecuting = false;
	
	/**
	 * Default constructor, for the single instance that ever exists.
	 */
	private SampleService()
	{
		setServiceExecuting(false);
	}

	/**
	 * Thread-safe update to the service control flag, sets or clears it.
	 * 
	 * @param executingNow value to be stored in the flag
	 */	
	private synchronized void setServiceExecuting(boolean executingNow)
	{
		serviceExecuting = executingNow;
	}

	/**
	 * Thread-safe accessor for the service control flag.
	 * 
	 * @return true or false value of the control flag at this point in time
	 */	
	private synchronized boolean isServiceExecuting()
	{
		return serviceExecuting;
	}

	/**
	 * Start service execution, until such time as control flag is cleared.
	 * Does little other than output message and sleep one second at a time
	 * in a continuous loop, which terminates if/when the flag gets reset.
	 */	
	private void execute()
	{
		System.out.println("Starting service execution");
		
		setServiceExecuting(true);
		while (isServiceExecuting())
		{
			try
			{
				doSomething();
				Thread.sleep(1000); // wait one second on each loop iteration
			}
			catch (InterruptedException ignored)
			{
			}
		}
		System.out.println("Ended service execution");
	}

	/**
	 * Perform a little bit of cpu-crunching just to provide some unit of work.
	 * Note that this does not use objects, so heap sizing unlikely to be
	 * affected and garbage collection will achieve very little once the
	 * temporary objects (runtime ref and string concatenations) from startup
	 * information logging have been cleaned up (other strings are fixed).
	 * The finishing heap size will always be off a bit though, as shutdown
	 * gets some more temporaries which would are not cleaned up right away.
	 */
	private void doSomething()
	{
		int x = 0;
		for (int i = 0; i < 1001; i++)
		{
			x += i;
			for (int j = 0; j < 42; j++)
			{
				x += j;
			}
		}
		System.out.println("Service calculation = " + x);
	}

	/**
	 * End service execution, by resetting the control flag that is checked on
	 * each iteration of the service processing loop.
	 */	
	private void abort()
	{
		System.out.println("Aborting service execution");
		setServiceExecuting(false);
	}

}