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

import playn.core.PlayN;
import playn.core.util.Callback;

public class SimpleTest1 extends ATest {

	public void init() {
		String[] paths = new String[] { "goblins/", "goblins/", "spineboy/" };
		String[] names = new String[] { "goblins", "goblins-ffd", "spineboy" };
		for (int i = 0; i < paths.length; i++) {
			final String path = paths[i];
			final String name = names[i];

			final String atlasFile = path + name + ".atlas";
			PlayN.assets().getBytes(atlasFile, new Callback<byte[]>() {
				@Override
				public void onFailure(Throwable cause) {
					PlayN.log().error("Error while loading " + atlasFile, cause);
				}

				@Override
				public void onSuccess(byte[] result) {
					Atlas _temp = null;
					try {
						_temp = new Atlas(new ByteArrayInputStream(result), path, new PlayNTextureLoader(PlayN.graphics().rootLayer()));
					} catch (Exception e) {
						PlayN.log().error("Error while loading " + atlasFile, e);
					}
					final Atlas atlas = _temp;

					final String spineJsonFile = path + name + ".json";
					PlayN.assets().getText(spineJsonFile, new Callback<String>() {
						@Override
						public void onFailure(Throwable cause) {
							PlayN.log().error("Error while loading " + spineJsonFile, cause);
						}

						@Override
						public void onSuccess(String result) {
							final SkeletonJson json = new SkeletonJson(atlas);
							final SkeletonData data1 = json.readSkeletonData(name, convert(PlayN.json().parse(result)));
							assert data1 != null;
							PlayN.log().info("SkeletonJSON successfully loaded for " + name);

							/*final String spineBinaryFile = path + name + ".skel";
							PlayN.assets().getBytes(spineBinaryFile, new Callback<byte[]>() {
								@Override
								public void onFailure(Throwable cause) {
									PlayN.log().error("Error while loading " + spineBinaryFile, cause);
								}

								@Override
								public void onSuccess(byte[] result) {
									final SkeletonBinary binary = new SkeletonBinary(atlas);
									final SkeletonData data2 = binary.readSkeletonData(name, new ByteArrayInputStream(result));
									PlayN.log().info("SkeletonBinary successfully loaded for " + name);
								}
							});*/
						}
					});
				}
			});
		}
	}

	@Override
	public void update(int delta) {
		super.update(delta);
	}

	@Override
	public void paint(float alpha) {
		super.paint(alpha);
	}

	public static final void main(final String[] args) {
		run(new SimpleTest1());
	}

}
