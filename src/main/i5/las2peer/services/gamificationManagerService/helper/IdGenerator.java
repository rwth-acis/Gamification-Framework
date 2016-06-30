package i5.las2peer.services.gamificationManagerService.helper;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * From las2peer-Microblog-Service
 * Generates a random ID using SecureRandom. IDs are NOT Base64, but use same principle for shorter string-length
 */
public class IdGenerator {
	/**
	 *
	 * @return a random string from a 64 bit number
	 */
	public static String generateId() {
		SecureRandom prng = new SecureRandom();
		Long randomNum = prng.nextLong();
		return getIdString(randomNum);
	}

	private static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk=mnopqrstuvwxyz0123456789+l";

	/**
	 * Converts a long to a short string
	 * 
	 * @param id number to convert
	 * @return Base64 like string representation
	 */
	public static String getIdString(long id) {

		byte[] bytes = new byte[9];
		StringBuilder sb = new StringBuilder();
		System.arraycopy(ByteBuffer.allocate(8).putLong(id).array(), 0, bytes, 0, 8);

		int n1 = (bytes[0] << 16) | (bytes[1] << 8) | (bytes[2]);
		int n2 = (bytes[3] << 16) | (bytes[4] << 8) | (bytes[5]);
		int n3 = (bytes[6] << 16) | (bytes[7] << 8) | (bytes[8]);

		int n11 = (n1 >> 18) & 63, n12 = (n1 >> 12) & 63, n13 = (n1 >> 6) & 63, n14 = n1 & 63;
		int n21 = (n2 >> 18) & 63, n22 = (n2 >> 12) & 63, n23 = (n2 >> 6) & 63, n24 = n2 & 63;
		int n31 = (n3 >> 18) & 63, n32 = (n3 >> 12) & 63, n33 = (n3 >> 6) & 63;

		sb.append(chars.charAt(n11));
		sb.append(chars.charAt(n12));
		sb.append(chars.charAt(n13));
		sb.append(chars.charAt(n14));

		sb.append(chars.charAt(n21));
		sb.append(chars.charAt(n22));
		sb.append(chars.charAt(n23));
		sb.append(chars.charAt(n24));

		sb.append(chars.charAt(n31));
		sb.append(chars.charAt(n32));
		sb.append(chars.charAt(n33));

		return sb.toString();
	}
}
