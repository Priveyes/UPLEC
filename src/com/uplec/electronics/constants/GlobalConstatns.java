package com.uplec.electronics.constants;

public class GlobalConstatns {

	/**
	 * Maximum allowed number
	 */
	public static int			MAX_NUMBER			= 199;

	/**
	 * Log tag string
	 */
	public static final String	LOG_TAG				= "UPLEC";

	/**
	 * Indicates is NFC supported by device (true by default)
	 */
	public static boolean		IS_NFC_SUPPORTED	= true;

	/**
	 * Writing NFC tag state
	 * 0 -> decrement
	 * 1 -> increment
	 * 2 -> constant
	 * 
	 * @author ihorkarpachev
	 */
	public interface WRITING_STATE {

		public static int	CONSTANT	= 2;
		public static int	INCREMENT	= 1;
		public static int	DECREMENT	= 0;
	}
}
