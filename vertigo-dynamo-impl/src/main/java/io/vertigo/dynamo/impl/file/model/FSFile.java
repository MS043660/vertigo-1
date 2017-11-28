/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.impl.file.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

/**
 * Représentation d'un fichier créé à partir d'un FileSystem.
 *
 * @author npiedeloup
 */
public final class FSFile extends AbstractVFile {
	private static final long serialVersionUID = 1L;
	private final File file;

	/**
	 * Constructor.
	 * Associe un fichier à des méta-données
	 * @param fileName Nom d'origine du fichier
	 * @param mimeType Type mime du fichier
	 * @param file Fichier en lui même (non null)
	 */
	public FSFile(final String fileName, final String mimeType, final File file) {
		super(fileName, mimeType, Instant.ofEpochMilli(file.lastModified()), file.length());
		//-----
		this.file = file;
	}

	/**
	 * @return Fichier en lui même	 */
	public File getFile() {
		return file;
	}

	/** {@inheritDoc} */
	@Override
	public InputStream createInputStream() throws IOException {
		return new java.io.FileInputStream(file);
		//Exemple de code où on recrée à chaque fois le inputStream
		//return new java.io.FileInputStream(file);

		//Exemple de code où on bufferise la première fois puis on le reset.
		//		if (inputStream == null) {
		//			inputStream = new BufferedInputStream(new java.io.FileInputStream(file));
		//			inputStream.mark(Integer.MAX_VALUE);
		//		} else {
		//			inputStream.reset();
		//		}
		//return inputStream;
	}

}
