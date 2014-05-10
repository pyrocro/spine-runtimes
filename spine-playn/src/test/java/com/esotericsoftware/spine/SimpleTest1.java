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

import playn.core.Color;
import playn.core.Keyboard;
import playn.core.Keyboard.Event;
import playn.core.Keyboard.TypedEvent;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.util.Callback;
import tripleplay.util.Hud;

import com.esotericsoftware.spine.attachments.RegionAttachment;

public class SimpleTest1 extends ATest implements Keyboard.Listener {

	Atlas altas;
	Skeleton skeleton;
	AnimationState state;
	boolean loaded = false;

	private Hud.Stock hud;

	public void init() {
		hud = new Hud.Stock();
		hud.layer.setDepth(Short.MAX_VALUE);
		PlayN.graphics().rootLayer().add(hud.layer);

		final String atlasPath = "spineboy/spineboy.atlas";
		final String skeletonPath = "spineboy/spineboy.json";

		// Load the Atlas.
		SpineLoader.getAtlas(atlasPath, new Callback<Atlas>() {
			@Override
			public void onFailure(Throwable cause) {
				PlayN.log().error("Error while loading Atlas " + atlasPath, cause);
			}

			@Override
			public void onSuccess(final Atlas result) {
				altas = result;

				// Load the Skeleton, scaling it at 60% of its original size.
				SpineLoader.getSkeleton(skeletonPath, 0.6f, SimpleTest1.this.altas, new Callback<Skeleton>() {
					@Override
					public void onFailure(Throwable cause) {
						PlayN.log().error("Error while loading Skeleton " + skeletonPath, cause);
					}

					@Override
					public void onSuccess(final Skeleton result) {
						skeleton = result; // Skeleton holds skeleton state (bone positions, slot attachments, etc).

						skeleton.setX(PlayN.graphics().width() / 2f);
						skeleton.setY(PlayN.graphics().height() - 20);

						final AnimationStateData stateData = new AnimationStateData(skeleton.getData()); // Defines mixing (crossfading) between
																											// animations.
						stateData.setMix("walk", "jump", 0.2f);
						stateData.setMix("jump", "walk", 0.2f);

						stateData.setMix("run", "jump", 0.2f);
						stateData.setMix("jump", "run", 0.2f);

						stateData.setMix("walk", "run", 0.4f);
						stateData.setMix("run", "walk", 0.4f);

						state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
						state.setTimeScale(0.5f); // Slow all animations down to 50% speed.
						state.setAnimation(0, "walk", true);
						// state.addAnimation(0, "jump", false, 2); // Jump after 2 seconds.
						// state.addAnimation(0, "run", true, 0); // Run after the jump.

						// Add skeleton layer to the PlayN scene graph.
						PlayN.graphics().rootLayer().add(skeleton.rootLayer());

						PlayN.keyboard().setListener(SimpleTest1.this);
						loaded = true;
					}
				});
			}
		});
	}

	@Override
	public void update(int delta) {
		if (loaded) {
			hud.update(delta);
		}
	}

	int lastTick = -1;
	boolean premultipliedAlpha = true;

	@Override
	public void paint(float alpha) {
		if (loaded) {
			hud.paint();

			float delta = 0f;
			int tick = PlayN.tick();
			if (lastTick != -1) {
				delta = (tick - lastTick) / 1000f;
			}
			lastTick = tick;

			// FIXME premultiplied alpha ???
			if (skeleton.drawOrder != null) {
				for (Slot slot : skeleton.drawOrder) {
					if (slot.getAttachment() != null && slot.getAttachment() instanceof RegionAttachment) {
						RegionAttachment regionAttachment = ((RegionAttachment) slot.getAttachment());

						float a = skeleton.getA() * slot.getA() * regionAttachment.getA() * 255f;
						float multiplier = premultipliedAlpha ? a : 255f;

						Layer layer = ((RegionAttachment) slot.getAttachment()).getRendererObject();
						layer.setTint(Color.argb((int) a, (int) (skeleton.getR() * slot.getR() * regionAttachment.getR() * multiplier),
								(int) (skeleton.getG() * slot.getG() * regionAttachment.getG() * multiplier), (int) (skeleton.getB() * slot.getB()
										* regionAttachment.getB() * multiplier)));
					}
				}
			}

			state.update(delta);
			state.apply(skeleton);
		}
	}

	public static final void main(final String[] args) {
		run(new SimpleTest1());
	}

	boolean running = false;

	@Override
	public void onKeyDown(Event event) {
		switch (event.key()) {
		case UP:
			state.setAnimation(0, "jump", false);
			state.addAnimation(0, running ? "run" : "walk", true, 0);
			break;
		case LEFT:
			skeleton.setFlipX(true);
			break;
		case RIGHT:
			skeleton.setFlipX(false);
			break;
		case SPACE:
			state.setAnimation(0, running ? "walk" : "run", true);
			running = !running;
			break;
		default:
			break;
		}
	}

	@Override
	public void onKeyTyped(TypedEvent event) {
	}

	@Override
	public void onKeyUp(Event event) {
	}

}
