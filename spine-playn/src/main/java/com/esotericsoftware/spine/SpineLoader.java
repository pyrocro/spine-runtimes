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
	 * @param path
	 *            Path (inside the PlayN assets folder) where the Atlas file is located, ex: "spineboy/spineboy.atlas"
	 * @param parentLayer
	 *            Parent layer for layers associated to this
	 * @param callback
	 *            Callback when the Atlas has finished loading (or crashed...)
	 */
	public static final void getAtlas(final String path, final GroupLayer parentLayer, final Callback<Atlas> callback) {
		new Atlas(path, new SpineTextureLoader(parentLayer), callback);
	}

	/**
	 * Load asynchronously a Spine Skeleton from a file.
	 * 
	 * @param path
	 *            Path (inside the PlayN assets folder) where the Skeleton file is located, ex: "spineboy/spineboy.json". Note that if the file
	 *            extension is "json", a JSON loader will be used, else it will be considered as binary.
	 * @param atlas
	 *            Atlas
	 * @param callback
	 *            Callback when the skeleton has finished loading (or crashed...)
	 */
	public static final void getSkeleton(final String path, final Atlas atlas, final Callback<Skeleton> callback) {
		getSkeleton(path, 1f, atlas, callback);
	}

	/**
	 * Load asynchronously a Spine Skeleton from a file.
	 * 
	 * @param path
	 *            Path (inside the PlayN assets folder) where the Skeleton file is located, ex: "spineboy/spineboy.json". Note that if the file
	 *            extension is "json", a JSON loader will be used, else it will be considered as binary.
	 * @param scale
	 *            If you want to scale the Skeleton
	 * @param atlas
	 *            Atlas
	 * @param callback
	 *            Callback when the skeleton has finished loading (or crashed...)
	 */
	public static final void getSkeleton(final String path, final float scale, final Atlas atlas, final Callback<Skeleton> callback) {
		try {
			final String extension = FilenameUtils.getExtension(path);

			// Loading the skeleton Data using the correct method.
			if ("json".equalsIgnoreCase(extension)) {
				getSkeletonUsingJson(path, scale, atlas, callback);
			} else {
				getSkeletonUsingBinary(path, scale, atlas, callback);
			}

		} catch (Throwable cause) {
			callback.onFailure(cause);
		}
	}

	private static final void getSkeletonUsingBinary(final String path, final float scale, final Atlas atlas, final Callback<Skeleton> callback) {

		// Let's begin by fetching the content of the Skeleton file.
		PlayN.assets().getBytes(path, new Callback<byte[]>() {

			@Override
			public void onFailure(Throwable cause) {
				PlayN.log().error("Error while loading Spine BINARY skeleton file " + path, cause);
				callback.onFailure(cause);
			}

			@Override
			public void onSuccess(byte[] result) {

				// Load the Skeleton data using binary.
				final SkeletonBinary binary = new SkeletonBinary(new AtlasAttachmentLoader(atlas));
				binary.setScale(scale);
				final SkeletonData skeletonData = binary.readSkeletonData(FilenameUtils.getBaseName(path), new ByteArrayInputStream(result));

				// Return the Skeleton to the callback.
				callback.onSuccess(new Skeleton(skeletonData));
			}
		});
	}

	private static final void getSkeletonUsingJson(final String path, final float scale, final Atlas atlas, final Callback<Skeleton> callback) {

		// Let's begin by fetching the content of the Skeleton file.
		PlayN.assets().getText(path, new Callback<String>() {

			@Override
			public void onFailure(Throwable cause) {
				PlayN.log().error("Error while loading Spine JSON skeleton file " + path, cause);
				callback.onFailure(cause);
			}

			@Override
			public void onSuccess(String result) {

				// Parse the String result to obtain a PlayN JSON object.
				Json.Object jsonResult = PlayN.json().parse(result);

				// Load the Skeleton data using JSON.
				final SkeletonJson json = new SkeletonJson(new AtlasAttachmentLoader(atlas));
				json.setScale(scale);
				final SkeletonData skeletonData = json.readSkeletonData(FilenameUtils.getBaseName(path), jsonResult);

				// Return the Skeleton to the callback.
				callback.onSuccess(new Skeleton(skeletonData));
			}
		});
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

		public void load(final AtlasPage page, final String path, final Callback<Void> callback) {
			Image image = PlayN.assets().getImage(path);
			image.addCallback(new Callback<Image>() {
				@Override
				public void onFailure(Throwable cause) {
					callback.onFailure(cause);
				}

				@Override
				public void onSuccess(Image result) {
					page.rendererObject = PlayN.graphics().createImageLayer(result);
					page.width = (int) result.width();
					page.height = (int) result.height();
					PlayN.log().info("Successfully loaded image " + path + " ; size = " + result.width() + " x " + result.height());
					callback.onSuccess(null);
				}
			});
		}
	}

}
