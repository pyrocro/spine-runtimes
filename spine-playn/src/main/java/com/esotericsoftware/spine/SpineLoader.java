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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.Json;
import playn.core.PlayN;
import playn.core.util.Callback;

import com.esotericsoftware.spine.Atlas.AtlasPage;
import com.esotericsoftware.spine.Atlas.TextureLoader;
import com.esotericsoftware.spine.attachments.AtlasAttachmentLoader;

/**
 * A class for loading Spine skeleton using PlayN.
 * 
 * @author mbarbeaux
 */
public class SpineLoader {

	private SpineLoader() {
	}

	/**
	 * Load asynchronously a Spine Atlas from a file.
	 * 
	 * @param directory
	 *            Directory (inside the PlayN assets folder) where the Atlas file is located
	 * @param basename
	 *            Base name of the Atlas (e.g. if base name is "spineboy", it will locate the atlas file as "spineboy.atlas")
	 * @param parentLayer
	 *            Parent layer for layers associated to this
	 * @param callback
	 *            Callback when the Atlas has finished loading (or crashed...)
	 */
	public static final void getAtlas(final String directory, final String basename, final GroupLayer parentLayer, final Callback<Atlas> callback) {
		try {

			// Let's begin by fetching the content of the Atlas file.
			final String atlasFile = FilenameUtils.concat(directory, basename + ".atlas");
			PlayN.assets().getBytes(atlasFile, new Callback<byte[]>() {

				@Override
				public void onFailure(Throwable cause) {
					callback.onFailure(cause);
				}

				@Override
				public void onSuccess(byte[] result) {

					// Now we can load the Atlas object.
					final Atlas atlas = new Atlas(new ByteArrayInputStream(result), directory, new SpineTextureLoader(parentLayer));
					callback.onSuccess(atlas);
				}
			});

		} catch (Throwable cause) {
			callback.onFailure(cause);
		}
	}

	/**
	 * Load asynchronously a Spine Skeleton from a file.
	 * 
	 * @param directory
	 *            Directory (inside the PlayN assets folder) where the Skeleton files are located
	 * @param basename
	 *            Base name of the Skeleton (e.g. if base name is "spineboy", it will locate the atlas file as "spineboy.atlas" and the skeleton file
	 *            as "spineboy.json" or "spineboy.skel", depending it is binary or not)
	 * @param binary
	 *            Indicate if the Skeleton must be loaded as a binary *.skel file (TRUE) or as a JSON *.json file (FALSE)
	 * @param atlas
	 *            Atlas
	 * @param callback
	 *            Callback when the skeleton has finished loading (or crashed...)
	 */
	public static final void getSkeleton(final String directory, final String basename, final boolean binary, final Atlas atlas,
			final Callback<Skeleton> callback) {
		getSkeleton(directory, basename, 1f, binary, atlas, callback);
	}

	/**
	 * Load asynchronously a Spine Skeleton from a file.
	 * 
	 * @param directory
	 *            Directory (inside the PlayN assets folder) where the Skeleton files are located
	 * @param basename
	 *            Base name of the Skeleton (e.g. if base name is "spineboy", it will locate the atlas file as "spineboy.atlas" and the skeleton file
	 *            as "spineboy.json" or "spineboy.skel", depending it is binary or not)
	 * @param scale
	 *            If you want to scale the Skeleton
	 * @param binary
	 *            Indicate if the Skeleton must be loaded as a binary *.skel file (TRUE) or as a JSON *.json file (FALSE)
	 * @param atlas
	 *            Atlas
	 * @param callback
	 *            Callback when the skeleton has finished loading (or crashed...)
	 */
	public static final void getSkeleton(final String directory, final String basename, final float scale, final boolean binary, final Atlas atlas,
			final Callback<Skeleton> callback) {
		try {

			// Loading the skeleton Data using the correct method.
			if (binary) {
				getSkeletonUsingBinary(directory, basename, scale, atlas, callback);
			} else {
				getSkeletonUsingJson(directory, basename, scale, atlas, callback);
			}

		} catch (Throwable cause) {
			callback.onFailure(cause);
		}
	}

	private static final void getSkeletonUsingBinary(final String directory, final String basename, final float scale, final Atlas atlas,
			final Callback<Skeleton> callback) {

		// Let's begin by fetching the content of the Skeleton file.
		final String skeletonFile = FilenameUtils.concat(directory, basename + ".skel");
		PlayN.assets().getBytes(skeletonFile, new Callback<byte[]>() {

			@Override
			public void onFailure(Throwable cause) {
				PlayN.log().error("Error while loading Spine skeleton file " + skeletonFile, cause);
				callback.onFailure(cause);
			}

			@Override
			public void onSuccess(byte[] result) {

				// Load the Skeleton data using binary.
				final SkeletonBinary binary = new SkeletonBinary(new AtlasAttachmentLoader(atlas));
				binary.setScale(scale);
				final SkeletonData skeletonData = binary.readSkeletonData(basename, new ByteArrayInputStream(result));

				// Return the Skeleton to the callback.
				callback.onSuccess(new Skeleton(skeletonData));
			}
		});
	}

	private static final void getSkeletonUsingJson(final String directory, final String basename, final float scale, final Atlas atlas,
			final Callback<Skeleton> callback) {

		// Let's begin by fetching the content of the Skeleton file.
		final String skeletonFile = FilenameUtils.concat(directory, basename + ".json");
		PlayN.assets().getText(skeletonFile, new Callback<String>() {

			@Override
			public void onFailure(Throwable cause) {
				PlayN.log().error("Error while loading Spine skeleton file " + skeletonFile, cause);
				callback.onFailure(cause);
			}

			@Override
			public void onSuccess(String result) {

				// Load the Skeleton data using JSON.
				final SkeletonJson json = new SkeletonJson(new AtlasAttachmentLoader(atlas));
				json.setScale(scale);
				final SkeletonData skeletonData = json.readSkeletonData(basename, convert(PlayN.json().parse(result)));

				// Return the Skeleton to the callback.
				callback.onSuccess(new Skeleton(skeletonData));
			}
		});
	}

	/**
	 * Convert a PlayN Json.Object into an HashMap
	 * 
	 * @param o
	 * @return
	 */
	private static final HashMap<String, Object> convert(Json.Object o) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (String key : o.keys()) {
			if (o.isArray(key)) {
				map.put(key, convert(o.getArray(key)));
			} else if (o.isObject(key)) {
				map.put(key, convert(o.getObject(key)));
			} else if (o.isBoolean(key)) {
				map.put(key, o.getBoolean(key));
			} else if (o.isNumber(key)) {
				map.put(key, o.getNumber(key));
			} else if (o.isString(key)) {
				map.put(key, o.getString(key));
			} else {
				map.put(key, null);
			}
		}
		return map;
	}

	/**
	 * Convert a PlayN Json.Array into an ArrayList
	 * 
	 * @param a
	 * @return
	 */
	private static final ArrayList<Object> convert(Json.Array a) {
		ArrayList<Object> list = new ArrayList<Object>(a.length());
		for (int i = 0; i < a.length(); i++) {
			if (a.isArray(i)) {
				list.add(convert(a.getArray(i)));
			} else if (a.isObject(i)) {
				list.add(convert(a.getObject(i)));
			} else if (a.isBoolean(i)) {
				list.add(a.getBoolean(i));
			} else if (a.isNumber(i)) {
				list.add(a.getNumber(i));
			} else if (a.isString(i)) {
				list.add(a.getString(i));
			} else {
				list.add(null);
			}
		}
		return list;
	}

	/**
	 * Internal class for loading PlayN textures from Atlas file.
	 * 
	 * @author mbarbeaux
	 */
	private static class SpineTextureLoader implements TextureLoader {

		// Internal hidden Grouplayer for managing ImageLayers.
		GroupLayer groupLayer;

		public SpineTextureLoader(GroupLayer parent) {
			groupLayer = PlayN.graphics().createGroupLayer();
			groupLayer.setVisible(false);
			parent.add(groupLayer);
		}

		public void unload(Object texture) {
			((ImageLayer) texture).destroy();
		}

		public void load(final AtlasPage page, final String path) {
			Image image = PlayN.assets().getImageSync(path); // FIXME make async for HTML5, but we need width and height as soon as possible, so... ??
			page.rendererObject = PlayN.graphics().createImageLayer(image);
			page.width = (int) image.width();
			page.height = (int) image.height();
			PlayN.log().info("Successfully loaded image " + path + " ; size = " + image.width() + " x " + image.height());
		}
	}

}
