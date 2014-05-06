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

import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.PlayN;
import playn.core.util.Callback;

import com.esotericsoftware.spine.Atlas.AtlasPage;
import com.esotericsoftware.spine.Atlas.TextureLoader;

/**
 * Custom {@link TextureLoader} for the PlayN platform.
 * Textures in PlayN are stored as Images, which are then stored in the scene graphs as ImageLayers.
 * 
 * @author mbarbeaux
 */
public class PlayNTextureLoader implements TextureLoader {

	// Internal hidden Grouplayer for managing ImageLayers.
	GroupLayer groupLayer;

	public PlayNTextureLoader(GroupLayer parent) {
		groupLayer = PlayN.graphics().createGroupLayer();
		groupLayer.setVisible(false);
		parent.add(groupLayer);
	}

	public void unload(Object texture) {
		((ImageLayer) texture).destroy();
	}

	public void load(final AtlasPage page, final String path) {
		Image image = PlayN.assets().getImage(path);
		ImageLayer layer = PlayN.graphics().createImageLayer(image);
		groupLayer.add(layer);
		page.rendererObject = layer;
		image.addCallback(new Callback<Image>() {
			@Override
			public void onFailure(Throwable cause) {
				PlayN.log().error("Error while loading image " + path, cause);
			}

			@Override
			public void onSuccess(Image result) {
				PlayN.log().info("Successfully loaded image " + path + " ; size = " + result.width() + " x " + result.height());
			}
		});
	}

}
