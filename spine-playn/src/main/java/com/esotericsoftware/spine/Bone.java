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

import playn.core.GroupLayer;
import pythagoras.f.FloatMath;

public class Bone {
	static public boolean yDown;

	final BoneData data;
	final Bone parent;

	ArrayList<GroupLayer> layers = new ArrayList<GroupLayer>();

	/**
	 * @param parent
	 *            May be null.
	 */
	public Bone(BoneData data, Bone parent) {
		if (data == null)
			throw new IllegalArgumentException("data cannot be null.");
		this.data = data;
		this.parent = parent;

		setToSetupPose();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param parent
	 *            May be null.
	 */
	public Bone(Bone bone, Bone parent) {
		if (bone == null)
			throw new IllegalArgumentException("bone cannot be null.");
		this.parent = parent;
		data = bone.data;

		setX(bone.getX());
		setY(bone.getY());
		setRotation(bone.getRotation());
		setScaleX(bone.getScaleX());
		setScaleY(bone.getScaleY());
	}

	public void setToSetupPose() {
		BoneData data = this.data;

		setX(data.x);
		setY(data.y);
		setRotation(data.rotation);
		setScaleX(data.scaleX);
		setScaleY(data.scaleY);
	}

	public BoneData getData() {
		return data;
	}

	public Bone getParent() {
		return parent;
	}

	public float getX() {
		return layers.size() > 0 ? layers.get(0).tx() : 0f;
	}

	public void setX(float x) {
		for (GroupLayer temp : layers) {
			temp.setTx(x);
		}
	}

	public float getY() {
		return layers.size() > 0 ? layers.get(0).ty() : 0f;
	}

	public void setY(float y) {
		for (GroupLayer temp : layers) {
			temp.setTy(y);
		}
	}

	public float getRotation() {
		return FloatMath.toDegrees(layers.size() > 0 ? layers.get(0).rotation() : 0f);
	}

	public void setRotation(float degrees) {
		float rad = FloatMath.toRadians(degrees);
		for (GroupLayer temp : layers) {
			temp.setRotation(rad);
		}
	}

	public float getScaleX() {
		return layers.size() > 0 ? layers.get(0).scaleX() : 0f;
	}

	public void setScaleX(float scaleX) {
		for (GroupLayer temp : layers) {
			temp.setScaleX(scaleX);
		}
	}

	public float getScaleY() {
		return layers.size() > 0 ? layers.get(0).scaleY() : 0f;
	}

	public void setScaleY(float scaleY) {
		for (GroupLayer temp : layers) {
			temp.setScaleY(scaleY);
		}
	}

	public String toString() {
		return data.name;
	}

	public void addGroupLayer(final GroupLayer layer) {
		if (layers.size() > 0) {
			GroupLayer temp = layers.get(0);
			layer.setTx(temp.tx());
			layer.setTy(temp.ty());
			layer.setRotation(temp.rotation());
			layer.setScaleX(temp.scaleX());
			layer.setScaleY(temp.scaleY());
		}
		layers.add(layer);
	}

}
