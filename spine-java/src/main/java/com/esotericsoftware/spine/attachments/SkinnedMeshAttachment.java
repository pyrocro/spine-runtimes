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

package com.esotericsoftware.spine.attachments;

import java.util.ArrayList;

import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Slot;

/** Attachment that displays a texture region. */
public class SkinnedMeshAttachment extends Attachment {

	int[] bones;
	float[] weights, uvs, regionUVs;
	int[] triangles;
	float regionOffsetX, regionOffsetY, regionWidth, regionHeight, regionOriginalWidth, regionOriginalHeight;
	float regionU, regionV, regionU2, regionV2;
	boolean regionRotate;
	float r = 1, g = 1, b = 1, a = 1;

	String path;
	Object renderObject;

	int hullLength;
	int[] edges;
	float width;
	float height;

	public SkinnedMeshAttachment(String name) {
		super(name);
	}

	public void updateUVs() {
		float u = regionU, v = regionV, width = regionU2 - regionU, height = regionV2 - regionV;
		float[] regionUVs = this.regionUVs;
		if (this.uvs == null || this.uvs.length != regionUVs.length)
			this.uvs = new float[regionUVs.length];
		float[] uvs = this.uvs;
		if (regionRotate) {
			for (int i = 0, n = uvs.length; i < n; i += 2) {
				uvs[i] = u + regionUVs[i + 1] * width;
				uvs[i + 1] = v + height - regionUVs[i] * height;
			}
		} else {
			for (int i = 0, n = uvs.length; i < n; i += 2) {
				uvs[i] = u + regionUVs[i] * width;
				uvs[i + 1] = v + regionUVs[i + 1] * height;
			}
		}
	}
	
	public void computeWorldVertices (float x, float y, Slot slot, float[] worldVertices) {
		ArrayList<Bone> skeletonBones = slot.getSkeleton().getBones();
		float[] weights = this.weights;
		int[] bones = this.bones;
		if (slot.getAttachmentVerticesCount() == 0) {
			for (int w = 0, v = 0, b = 0, n = bones.length; v < n; w += 2) {
				float wx = 0, wy = 0;
				int nn = bones[v++] + v;
				for (; v < nn; v++, b += 3) {
					Bone bone = (Bone)skeletonBones.get(bones[v]);
					float vx = weights[b], vy = weights[b + 1], weight = weights[b + 2];
					wx += (vx * bone.getM00() + vy * bone.getM01() + bone.getWorldX()) * weight;
					wy += (vx * bone.getM10() + vy * bone.getM11() + bone.getWorldY()) * weight;
				}
				worldVertices[w] = wx + x;
				worldVertices[w + 1] = wy + y;
			}
		} else {
			float[] ffd = slot.getAttachmentVertices();
			for (int w = 0, v = 0, b = 0, f = 0, n = bones.length; v < n; w += 2) {
				float wx = 0, wy = 0;
				int nn = bones[v++] + v;
				for (; v < nn; v++, b += 3, f += 2) {
					Bone bone = (Bone)skeletonBones.get(bones[v]);
					float vx = weights[b] + ffd[f], vy = weights[b + 1] + ffd[f + 1], weight = weights[b + 2];
					wx += (vx * bone.getM00() + vy * bone.getM01() + bone.getWorldX()) * weight;
					wy += (vx * bone.getM10() + vy * bone.getM11() + bone.getWorldY()) * weight;
				}
				worldVertices[w] = wx + x;
				worldVertices[w + 1] = wy + y;
			}
		}
	}

	public int[] getBones() {
		return bones;
	}

	public void setBones(int[] bones) {
		this.bones = bones;
	}

	public float[] getWeights() {
		return weights;
	}

	public void setWeights(float[] weights) {
		this.weights = weights;
	}

	public float[] getRegionUVs() {
		return regionUVs;
	}

	public void setRegionUVs(float[] regionUVs) {
		this.regionUVs = regionUVs;
	}

	public float[] getUvs() {
		return uvs;
	}

	public void setUvs(float[] uvs) {
		this.uvs = uvs;
	}

	public int[] getTriangles() {
		return triangles;
	}

	public void setTriangles(int[] triangles) {
		this.triangles = triangles;
	}

	public float getR() {
		return r;
	}

	public void setR(float r) {
		this.r = r;
	}

	public float getG() {
		return g;
	}

	public void setG(float g) {
		this.g = g;
	}

	public float getB() {
		return b;
	}

	public void setB(float b) {
		this.b = b;
	}

	public float getA() {
		return a;
	}

	public void setA(float a) {
		this.a = a;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Object getRenderObject() {
		return renderObject;
	}

	public void setRenderObject(Object renderObject) {
		this.renderObject = renderObject;
	}

	public float getRegionU() {
		return regionU;
	}

	public void setRegionU(float regionU) {
		this.regionU = regionU;
	}

	public float getRegionV() {
		return regionV;
	}

	public void setRegionV(float regionV) {
		this.regionV = regionV;
	}

	public float getRegionU2() {
		return regionU2;
	}

	public void setRegionU2(float regionU2) {
		this.regionU2 = regionU2;
	}

	public float getRegionV2() {
		return regionV2;
	}

	public void setRegionV2(float regionV2) {
		this.regionV2 = regionV2;
	}

	public boolean isRegionRotate() {
		return regionRotate;
	}

	public void setRegionRotate(boolean regionRotate) {
		this.regionRotate = regionRotate;
	}

	public float getRegionOffsetX() {
		return regionOffsetX;
	}

	public void setRegionOffsetX(float regionOffsetX) {
		this.regionOffsetX = regionOffsetX;
	}

	public float getRegionOffsetY() {
		return regionOffsetY;
	}

	public void setRegionOffsetY(float regionOffsetY) {
		this.regionOffsetY = regionOffsetY;
	}

	public float getRegionWidth() {
		return regionWidth;
	}

	public void setRegionWidth(float regionWidth) {
		this.regionWidth = regionWidth;
	}

	public float getRegionHeight() {
		return regionHeight;
	}

	public void setRegionHeight(float regionHeight) {
		this.regionHeight = regionHeight;
	}

	public float getRegionOriginalWidth() {
		return regionOriginalWidth;
	}

	public void setRegionOriginalWidth(float regionOriginalWidth) {
		this.regionOriginalWidth = regionOriginalWidth;
	}

	public float getRegionOriginalHeight() {
		return regionOriginalHeight;
	}

	public void setRegionOriginalHeight(float regionOriginalHeight) {
		this.regionOriginalHeight = regionOriginalHeight;
	}

	public int getHullLength() {
		return hullLength;
	}

	public void setHullLength(int hullLength) {
		this.hullLength = hullLength;
	}

	public int[] getEdges() {
		return edges;
	}

	public void setEdges(int[] edges) {
		this.edges = edges;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

}
