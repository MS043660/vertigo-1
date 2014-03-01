package io.vertigo.commons.codec;

import io.vertigo.kernel.component.Manager;

import java.io.Serializable;


/**
 * Gestion centralisée des mécanismes de codage/décodage.
 * Tous les codecs sont threadSafe et StateLess.
 * 
 * - CSV null donne ""
 * - HTML null donne ""
 * - les fonctions de Hachage MD5 et SHA1 n'autorisent pas les null. 
 * - pour tous les autres cas null  donne null
 *
 * @author pchretien
 * @version $Id: CodecManager.java,v 1.4 2013/11/15 15:31:39 pchretien Exp $
 */
public interface CodecManager extends Manager {
	/**
	 * @return Codec HTML.
	 */
	Codec<String, String> getHtmlCodec();

	/**
	 * @return Encoder CSV.
	 */
	Encoder<String, String> getCsvEncoder();

	//-------------------------------------------------------------------------
	/**
	 * @return Encoder MD5. (128 bits) 
	 */
	Encoder<byte[], byte[]> getMD5Encoder();

	/**
	 * @return Encoder SHA-1. (160 bits) 
	 */
	Encoder<byte[], byte[]> getSha1Encoder();

	/**
	 * @return Encoder SHA-2. (256 bits) 
	 */
	Encoder<byte[], byte[]> getSha256Encoder();

	/**
	 * @return Codec Hexadecimal.
	 */
	Encoder<byte[], String> getHexEncoder();

	/**
	 * Le codage base 64 proposé autorise l'utilisation dans les URL en restreignant certains caractères.
	 * @return Codec Base 64.
	 */
	Codec<byte[], String> getBase64Codec();

	/**
	 * @return Codec cryptographique.
	 */
	Codec<byte[], byte[]> getTripleDESCodec();

	/**
	 * @return Codec cryptographique.
	 */
	Codec<byte[], byte[]> getAES128Codec();

	/**
	 * @return Codec de compression de données.
	 */
	Codec<byte[], byte[]> getCompressionCodec();

	/**
	 * @return Codec de sérialisation de données.
	 */
	Codec<Serializable, byte[]> getSerializationCodec();

	/**
	 * @return Codec de sérialisation compressée de données.
	 */
	Codec<Serializable, byte[]> getCompressedSerializationCodec();

}
