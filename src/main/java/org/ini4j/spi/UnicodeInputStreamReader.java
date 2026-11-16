/*
 * Copyright 2005,2009 Ivan SZKIBA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ini4j.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * A reader for a stream whose character set is chosen by its byte order mark.
 *
 * <p>If the stream starts with a byte order mark of UTF-8, UTF-16 or UTF-32 (the ones the JVM
 * supports), the mark is skipped and its character set is used. Otherwise the default character set
 * that was given to the constructor is used. The check runs when the stream is first read or
 * closed.
 *
 * @since 0.5.2
 */
class UnicodeInputStreamReader extends Reader {
  private static final int BOM_SIZE = 4;

  private enum Bom {
    UTF32BE("UTF-32BE", new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF}),
    UTF32LE("UTF-32LE", new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00}),
    UTF16BE("UTF-16BE", new byte[] {(byte) 0xFE, (byte) 0xFF}),
    UTF16LE("UTF-16LE", new byte[] {(byte) 0xFF, (byte) 0xFE}),
    UTF8("UTF-8", new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
    private final byte[] _bytes;
    private Charset _charset;

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    Bom(String charsetName, byte[] bytes) {
      try {
        _charset = Charset.forName(charsetName);
      } catch (Exception x) {
        _charset = null;
      }

      _bytes = bytes;
    }

    private static Bom find(byte[] data) {
      Bom ret = null;

      for (Bom bom : values()) {
        if (bom.supported() && bom.match(data)) {
          ret = bom;

          break;
        }
      }

      return ret;
    }

    private boolean match(byte[] data) {
      boolean ok = true;

      for (int i = 0; i < _bytes.length; i++) {
        if (data[i] != _bytes[i]) {
          ok = false;

          break;
        }
      }

      return ok;
    }

    private boolean supported() {
      return _charset != null;
    }
  }

  private final Charset _defaultEncoding;
  private InputStreamReader _reader;
  private final PushbackInputStream _stream;

  UnicodeInputStreamReader(InputStream in, Charset defaultEnc) {
    _stream = new PushbackInputStream(in, BOM_SIZE);
    _defaultEncoding = defaultEnc;
  }

  @Override
  public void close() throws IOException {
    init();
    _reader.close();
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    init();

    return _reader.read(cbuf, off, len);
  }

  /**
   * Reads the first bytes of the stream and looks for a byte order mark.
   *
   * <p>Up to four bytes are read. If they start with a byte order mark, the mark is skipped and the
   * bytes after it are pushed back to the stream. Otherwise all the bytes that were read are pushed
   * back. The method does nothing after the first call. The number of bytes to push back after a
   * byte order mark assumes that four bytes were read. If the input is shorter and starts with a
   * byte order mark, bytes of the mark can be pushed back and then read as text.
   *
   * @throws IOException if reading fails
   */
  protected void init() throws IOException {
    if (_reader != null) {
      return;
    }

    Charset encoding;
    byte[] data = new byte[BOM_SIZE];
    int n;
    int unread;

    n = _stream.read(data, 0, data.length);
    Bom bom = Bom.find(data);

    if (bom == null) {
      encoding = _defaultEncoding;
      unread = n;
    } else {
      encoding = bom._charset;
      unread = data.length - bom._bytes.length;
    }

    if (unread > 0) {
      _stream.unread(data, n - unread, unread);
    }

    _reader = new InputStreamReader(_stream, encoding);
  }
}
