/******************************************************************************
 * Spine Runtimes Software License
 * Version 2.1
 * 
 * Copyright (c) 2013, Esoteric Software
 * All rights reserved.
 * 
 * You are granted a perpetual, non-exclusive, non-sublicensable and
 * non-transferable license to install, execute and perform the Spine Runtimes
 * Software (the "Software") solely for internal use. Without the written
 * permission of Esoteric Software (typically granted by licensing Spine), you
 * may not (a) modify, translate, adapt or otherwise create derivative works,
 * improvements of the Software or develop new applications using the Software
 * or (b) remove, delete, alter or obscure any trademarks or any copyright,
 * trademark, patent or other intellectual property or proprietary rights
 * notices on or in the Software, including any copy thereof. Redistributions
 * in binary or source form must include this license and terms.
 * 
 * THIS SOFTWARE IS PROVIDED BY ESOTERIC SOFTWARE "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL ESOTERIC SOFTARE BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

package com.esotericsoftware.spine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import playn.core.PlayN;
import playn.core.util.Callback;

public class Atlas {
	static final String[] tuple = new String[4];

	ArrayList<AtlasPage> pages = new ArrayList<AtlasPage>();
	ArrayList<AtlasRegion> regions = new ArrayList<AtlasRegion>();
	TextureLoader textureLoader;

	/**
	 * Use SpineLoader.getAtlas(...) instead
	 */
	Atlas(final String path, final TextureLoader textureLoader, final Callback<Atlas> callback) {
		if (textureLoader == null) {
			throw new IllegalArgumentException("textureLoader cannot be null.");
		}
		this.textureLoader = textureLoader;

		// Load asynchronously the content of the atlas file.
		PlayN.assets().getText(path, new Callback<String>() {

			@Override
			public void onFailure(final Throwable cause) {
				callback.onFailure(cause);
			}

			@Override
			public void onSuccess(final String result) {

				// Data loaded, now parse atlas file.
				final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.getBytes())));
				Atlas.this.load(reader, null, FilenameUtils.getPath(path), callback);
			}
		});
	}

	/**
	 * Copy constructor
	 */
	public Atlas(final ArrayList<AtlasPage> pages, final ArrayList<AtlasRegion> regions) {
		this.pages = pages;
		this.regions = regions;
		this.textureLoader = null;
	}

	private void load(final BufferedReader reader, final AtlasPage page, final String imagesDir, final Callback<Atlas> callback) {
		try {
			final String line = reader.readLine();

			// EOF
			if (line == null) {
				reader.close();
				callback.onSuccess(Atlas.this);
			}

			// Try to continue reading file, using a empty AtlasPage
			else if (line.trim().length() == 0) {
				this.load(reader, null, imagesDir, callback);
			}

			// Load a new AtlasPage
			else if (page == null) {
				this.loadPage(line, reader, imagesDir, new Callback<AtlasPage>() {

					@Override
					public void onFailure(final Throwable cause) {
						callback.onFailure(cause);
					}

					@Override
					public void onSuccess(final AtlasPage result) {

						// AtlasPage successfully loaded, keep reading
						try {
							Atlas.this.load(reader, result, imagesDir, callback);
						} catch (final Throwable cause) {
							callback.onFailure(cause);
						}
					}
				});
			}

			// Load AtlasRegion
			else {
				final AtlasRegion region = new AtlasRegion();
				region.name = line;
				region.page = page;

				region.rotate = Boolean.valueOf(Atlas.readValue(reader));

				Atlas.readTuple(reader);
				final int x = Integer.parseInt(tuple[0]);
				final int y = Integer.parseInt(tuple[1]);

				Atlas.readTuple(reader);
				final int width = Integer.parseInt(tuple[0]);
				final int height = Integer.parseInt(tuple[1]);

				region.u = x / (float) page.width;
				region.v = y / (float) page.height;
				if (region.rotate) {
					region.u2 = (x + height) / (float) page.width;
					region.v2 = (y + width) / (float) page.height;
				} else {
					region.u2 = (x + width) / (float) page.width;
					region.v2 = (y + height) / (float) page.height;
				}
				region.x = x;
				region.y = y;
				region.width = Math.abs(width);
				region.height = Math.abs(height);

				if (Atlas.readTuple(reader) == 4) { // split is optional
					region.splits = new int[] { Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]), Integer.parseInt(tuple[2]),
							Integer.parseInt(tuple[3]) };

					if (Atlas.readTuple(reader) == 4) { // pad is optional, but only present with splits
						region.pads = new int[] { Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]), Integer.parseInt(tuple[2]),
								Integer.parseInt(tuple[3]) };

						Atlas.readTuple(reader);
					}
				}

				region.originalWidth = Integer.parseInt(tuple[0]);
				region.originalHeight = Integer.parseInt(tuple[1]);

				Atlas.readTuple(reader);
				region.offsetX = Integer.parseInt(tuple[0]);
				region.offsetY = Integer.parseInt(tuple[1]);

				region.index = Integer.parseInt(Atlas.readValue(reader));

				this.regions.add(region);

				// Keep reading...
				this.load(reader, page, imagesDir, callback);
			}
		} catch (final Throwable cause) {
			callback.onFailure(cause);
		}
	}

	private void loadPage(final String line, final BufferedReader reader, final String imagesDir, final Callback<AtlasPage> callback) {
		try {
			final AtlasPage page = new AtlasPage();
			page.name = line;

			if (Atlas.readTuple(reader) == 2) { // size is only optional for an atlas packed with an old TexturePacker.
				page.width = Integer.parseInt(tuple[0]);
				page.height = Integer.parseInt(tuple[1]);
				Atlas.readTuple(reader);
			}
			page.format = Format.valueOf(tuple[0]);

			Atlas.readTuple(reader);
			page.minFilter = TextureFilter.valueOf(tuple[0]);
			page.magFilter = TextureFilter.valueOf(tuple[1]);

			final String direction = Atlas.readValue(reader);
			page.uWrap = TextureWrap.ClampToEdge;
			page.vWrap = TextureWrap.ClampToEdge;
			if (direction.equals("x")) {
				page.uWrap = TextureWrap.Repeat;
			} else if (direction.equals("y")) {
				page.vWrap = TextureWrap.Repeat;
			} else if (direction.equals("xy")) {
				page.uWrap = page.vWrap = TextureWrap.Repeat;
			}

			// Load texture
			this.textureLoader.load(page, FilenameUtils.concat(imagesDir, line), new Callback<Void>() {
				@Override
				public void onFailure(final Throwable cause) {
					callback.onFailure(cause);
				}

				@Override
				public void onSuccess(final Void result) {
					Atlas.this.pages.add(page);
					callback.onSuccess(page);
				}
			});
		} catch (final Throwable cause) {
			callback.onFailure(cause);
		}
	}

	static String readValue(final BufferedReader reader) throws IOException {
		final String line = reader.readLine();
		final int colon = line.indexOf(':');
		if (colon == -1) {
			throw new RuntimeException("Invalid line: " + line);
		}
		return line.substring(colon + 1).trim();
	}

	/** Returns the number of tuple values read (1, 2 or 4). */
	static int readTuple(final BufferedReader reader) throws IOException {
		final String line = reader.readLine();
		final int colon = line.indexOf(':');
		if (colon == -1) {
			throw new RuntimeException("Invalid line: " + line);
		}
		int i = 0, lastMatch = colon + 1;
		for (i = 0; i < 3; i++) {
			final int comma = line.indexOf(',', lastMatch);
			if (comma == -1) {
				break;
			}
			tuple[i] = line.substring(lastMatch, comma).trim();
			lastMatch = comma + 1;
		}
		tuple[i] = line.substring(lastMatch).trim();
		return i + 1;
	}

	public void flipV() {
		final int rsize = this.regions.size();
		for (int i = 0; i < rsize; i++) {
			final AtlasRegion region = this.regions.get(i);
			region.v = 1 - region.v;
			region.v2 = 1 - region.v2;
		}
	}

	// / <summary>Returns the first region found with the specified name. This method uses string comparison to find the region, so the result
	// / should be cached rather than calling this method multiple times.</summary>
	// / <returns>The region, or null.</returns>
	public AtlasRegion findRegion(final String name) {
		final int rsize = this.regions.size();
		for (int i = 0; i < rsize; i++) {
			if (this.regions.get(i).name.equals(name)) {
				return this.regions.get(i);
			}
		}
		return null;
	}

	public void dispose() {
		if (this.textureLoader == null) {
			return;
		}
		final int psize = this.pages.size();
		for (int i = 0; i < psize; i++) {
			this.textureLoader.unload(this.pages.get(i).rendererObject);
		}
	}

	public enum Format {
		Alpha, Intensity, LuminanceAlpha, RGB565, RGBA4444, RGB888, RGBA8888
	}

	public enum TextureFilter {
		Nearest, Linear, MipMap, MipMapNearestNearest, MipMapLinearNearest, MipMapNearestLinear, MipMapLinearLinear
	}

	public enum TextureWrap {
		MirroredRepeat, ClampToEdge, Repeat
	}

	public class AtlasPage {
		public String name;
		public Format format;
		public TextureFilter minFilter;
		public TextureFilter magFilter;
		public TextureWrap uWrap;
		public TextureWrap vWrap;
		public Object rendererObject;
		public int width, height;
	}

	public class AtlasRegion {
		public AtlasPage page;
		public String name;
		public int x, y, width, height;
		public float u, v, u2, v2;
		public float offsetX, offsetY;
		public int originalWidth, originalHeight;
		public int index;
		public boolean rotate;
		public int[] splits;
		public int[] pads;
	}

	public interface TextureLoader {
		void load(final AtlasPage page, final String path, final Callback<Void> callback);

		void unload(final Object texture);
	}

}
