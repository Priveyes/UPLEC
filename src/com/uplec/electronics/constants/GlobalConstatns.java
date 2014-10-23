package com.uplec.electronics.constants;

public class GlobalConstatns {

	/**
	 * Amount of digits in pattern
	 */
	public static final int		AMOUNT_OF_DIGITS_IN_PATTERN	= 3;

	/**
	 * Maximum allowed number
	 */
	public static int			MAX_NUMBER					= 199;

	/**
	 * Log tag string
	 */
	public static final String	LOG_TAG						= "UPLEC";

	/**
	 * Indicates is NFC supported by device (true by default)
	 */
	public static boolean		IS_NFC_SUPPORTED			= true;

	/**
	 * Writing NFC tag state
	 * <b>
	 * <p>
	 * 0 -> decrement</b>
	 * </p>
	 * <b>
	 * <p>
	 * 1 -> increment</b>
	 * </p>
	 * <b>
	 * <p>
	 * 2 -> constant</b>
	 * </p>
	 * 
	 * @author ihorkarpachev
	 */
	public interface WRITING_STATE {

		public static int	CONSTANT	= 2;
		public static int	INCREMENT	= 1;
		public static int	DECREMENT	= 0;
	}

	/**
	 * Control codes (pattern to be written into 4th word):
	 * <p>
	 * FF - no control code -> DO NOTHING
	 * </p>
	 * <p>
	 * 7F - update e-link display -> REMOVE OLD PATTERN APPLY NEW
	 * </p>
	 * <p>
	 * 00 - force clear of all segments to off - > RESET, 'UNKNOWN' pattern
	 * </p>
	 * 
	 * @author ihorkarpachev
	 */
	public interface CONTROL_COMMANDS {

		// Pattern for DO NOTHING
		public static byte[]	DO_NOTHING_0xFF	= { (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		// Pattern for UPDATE PATTERN
		public static byte[]	UPDATE_0x7F		= { (byte) 0x7F, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		// Pattern for FORCE CLEAR
		public static byte[]	CLEAR_0x00		= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	}

	/**
	 * Patterns for LEFTxx digits (0 | 1)
	 * 
	 * @author ihorkarpachev
	 */
	public interface LEFT_DIGITS_PATTERN {

		// Pattern for 0xx
		public static byte[]	_0	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		// Pattern for 1xx
		public static byte[]	_1	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xF8, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	}

	/**
	 * Patterns for xMIDDLEx digits (0..9)
	 * 
	 * @author ihorkarpachev
	 */
	public interface MIDDLE_DIGITS_PATTERN {

		// Pattern for x0x
		public static byte[]	_0	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		// Pattern for x1x
		public static byte[]	_1	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4E, (byte) 0x20, (byte) 0x00 };
		// Pattern for x2x
		public static byte[]	_2	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0xFC, (byte) 0x20, (byte) 0x00 };
		// Pattern for x3x
		public static byte[]	_3	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x7E, (byte) 0x20, (byte) 0x00 };
		// Pattern for x4x
		public static byte[]	_4	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x4E, (byte) 0x20, (byte) 0x00 };
		// Pattern for x5x
		public static byte[]	_5	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x76, (byte) 0x20, (byte) 0x00 };
		// Pattern for x6x
		public static byte[]	_6	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xF6, (byte) 0x20, (byte) 0x00 };
		// Pattern for x7x
		public static byte[]	_7	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x4E, (byte) 0x20, (byte) 0x00 };
		// Pattern for x8x
		public static byte[]	_8	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xFE, (byte) 0x20, (byte) 0x00 };
		// Pattern for x9x
		public static byte[]	_9	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0x7E, (byte) 0x20, (byte) 0x00 };
	}

	/**
	 * Patterns for xxRIGHT digits (0..9)
	 * 
	 * @author ihorkarpachev
	 */
	public interface RIGHT_DIGITS_PATTERN {

		// Pattern for xx0
		public static byte[]	_0	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		// Pattern for xx1
		public static byte[]	_1	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xF8 };
		// Pattern for xx2
		public static byte[]	_2	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x97, (byte) 0xBC };
		// Pattern for xx3
		public static byte[]	_3	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x93, (byte) 0xFC };
		// Pattern for xx4
		public static byte[]	_4	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xD0, (byte) 0xF8 };
		// Pattern for xx5
		public static byte[]	_5	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xD3, (byte) 0xEC };
		// Pattern for xx6
		public static byte[]	_6	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xD7, (byte) 0xEC };
		// Pattern for xx7
		public static byte[]	_7	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0xFC };
		// Pattern for xx8
		public static byte[]	_8	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xD7, (byte) 0xFC };
		// Pattern for xx9
		public static byte[]	_9	= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xD3, (byte) 0xFC };
	}
}