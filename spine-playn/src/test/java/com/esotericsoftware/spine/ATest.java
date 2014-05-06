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
import java.util.HashMap;

import playn.core.Game;
import playn.core.Json;
import playn.core.PlayN;
import playn.java.JavaPlatform;

public abstract class ATest extends Game.Default {

	// Default game update rate, in ms.
	private static final int UPDATE_RATE = 25;

	public ATest() {
		super(UPDATE_RATE);
	}
	
	protected HashMap<String, Object> convert(Json.Object o) {
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
	
	protected ArrayList<Object> convert(Json.Array a) {
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
	
	public static void run(final ATest test) {
		final JavaPlatform.Config config = new JavaPlatform.Config();
		config.width = 800;
		config.height = 600;
		final JavaPlatform platform = JavaPlatform.register(config);
		platform.setTitle(test.getClass().getSimpleName() + " PlayN Spine");
		platform.assets().setPathPrefix("");
		PlayN.run(test);
	}

}
