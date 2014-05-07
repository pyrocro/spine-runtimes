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

import java.util.ArrayList;

import playn.core.ImageLayer;
import playn.core.InternalTransform;
import playn.core.PlayN;
import playn.core.util.Callback;

import com.esotericsoftware.spine.Atlas.AtlasRegion;
import com.esotericsoftware.spine.attachments.RegionAttachment;

public class SimpleTest1 extends ATest {

	Atlas altas;
	Skeleton skeleton;
	AnimationState state;
	boolean loaded = false;

	public void init() {
		final String dir = "spineboy";
		final String basename = "spineboy";

		// Load the Atlas.
		SpineLoader.getAtlas(dir, basename, PlayN.graphics().rootLayer(), new Callback<Atlas>() {
			@Override
			public void onFailure(Throwable cause) {
				PlayN.log().error("Error while loading Atlas " + basename, cause);
			}

			@Override
			public void onSuccess(final Atlas result) {
				altas = result;

				// Load the Skeleton, scaling it at 60% of its original size.
				SpineLoader.getSkeleton(dir, basename, 1f, false, SimpleTest1.this.altas, new Callback<Skeleton>() {
					@Override
					public void onFailure(Throwable cause) {
						PlayN.log().error("Error while loading Skeleton " + basename, cause);
					}

					@Override
					public void onSuccess(final Skeleton result) {
						skeleton = result; // Skeleton holds skeleton state (bone positions, slot attachments, etc).

						skeleton.setX(PlayN.graphics().width() / 2);
						skeleton.setY(PlayN.graphics().height() / 2);

						AnimationStateData stateData = new AnimationStateData(skeleton.getData()); // Defines mixing (crossfading) between animations.
						stateData.setMix("walk", "jump", 0.2f);
						stateData.setMix("jump", "walk", 0.2f);

						state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
						state.setTimeScale(0.5f); // Slow all animations down to 50% speed.
						state.setAnimation(0, "walk", true);
						state.addAnimation(0, "jump", false, 2); // Jump after 2 seconds.
						state.addAnimation(0, "walk", true, 0); // Run after the jump.

						skeleton.updateWorldTransform();
						loaded = true;
					}
				});
			}
		});
	}

	@Override
	public void update(int delta) {
		if (loaded) {
		}
	}

	int lastTick = -1;

	@Override
	public void paint(float alpha) {
		if (loaded) {
			float delta = 0f;
			int tick = PlayN.tick();
			if (lastTick != -1) {
				delta = (tick - lastTick) / 1000f;
			}
			lastTick = tick;

			state.update(delta);
			state.apply(skeleton);
			skeleton.updateWorldTransform();

			ArrayList<Slot> drawOrder = skeleton.drawOrder;
			for (Slot slot : drawOrder) {
				if (slot.getAttachment() != null && slot.getAttachment() instanceof RegionAttachment) {
					RegionAttachment attachment = (RegionAttachment) slot.getAttachment();
					ImageLayer layer = (ImageLayer) ((AtlasRegion) attachment.getRendererObject()).page.rendererObject;

					float[] vertices = new float[8];
					attachment.computeWorldVertices(skeleton.x, skeleton.y, slot.getBone(), vertices);
					// item.vertexTL.Position.X = vertices[RegionAttachment.X1];
					// item.vertexTL.Position.Y = vertices[RegionAttachment.Y1];
					// item.vertexTL.Position.Z = 0;
					// item.vertexBL.Position.X = vertices[RegionAttachment.X2];
					// item.vertexBL.Position.Y = vertices[RegionAttachment.Y2];
					// item.vertexBL.Position.Z = 0;
					// item.vertexBR.Position.X = vertices[RegionAttachment.X3];
					// item.vertexBR.Position.Y = vertices[RegionAttachment.Y3];
					// item.vertexBR.Position.Z = 0;
					// item.vertexTR.Position.X = vertices[RegionAttachment.X4];
					// item.vertexTR.Position.Y = vertices[RegionAttachment.Y4];
					// item.vertexTR.Position.Z = 0;
					//
					float[] uvs = attachment.getUvs();
					// item.vertexTL.TextureCoordinate.X = uvs[RegionAttachment.X1];
					// item.vertexTL.TextureCoordinate.Y = uvs[RegionAttachment.Y1];
					// item.vertexBL.TextureCoordinate.X = uvs[RegionAttachment.X2];
					// item.vertexBL.TextureCoordinate.Y = uvs[RegionAttachment.Y2];
					// item.vertexBR.TextureCoordinate.X = uvs[RegionAttachment.X3];
					// item.vertexBR.TextureCoordinate.Y = uvs[RegionAttachment.Y3];
					// item.vertexTR.TextureCoordinate.X = uvs[RegionAttachment.X4];
					// item.vertexTR.TextureCoordinate.Y = uvs[RegionAttachment.Y4];

					int top = RegionAttachment.Y3;
					int left = RegionAttachment.X1;
					int right = RegionAttachment.X3;
					int bottom = RegionAttachment.Y1;
					// PlayN.log().debug(
					// vertices[left] + " - " + vertices[top] + " - " + vertices[right] + " - " + vertices[bottom] + " - " + uvs[left] + " - "
					// + uvs[top] + " - " + uvs[right] + " - " + uvs[bottom]);

					PlayN.graphics()
							.ctx()
							.quadShader(null)
							.prepareTexture(layer.image().ensureTexture(), layer.tint())
							.addQuad((InternalTransform) layer.transform(), vertices[left], vertices[top], vertices[right], vertices[bottom],
									uvs[left], uvs[top], uvs[right], uvs[bottom]);

				}
			}
		}
	}

	public static final void main(final String[] args) {
		run(new SimpleTest1());
	}

}
