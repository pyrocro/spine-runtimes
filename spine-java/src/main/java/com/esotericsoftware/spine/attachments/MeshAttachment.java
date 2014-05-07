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

import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Slot;

/** Attachment that displays a texture region. */
public class MeshAttachment extends Attachment {
	private String path;
	private float[] vertices, uvs, regionUVs;
	private short[] triangles;
	float regionOffsetX, regionOffsetY, regionWidth, regionHeight, regionOriginalWidth, regionOriginalHeight;
	float regionU, regionV, regionU2, regionV2;
	boolean regionRotate;
	float r = 1f, g = 1f, b = 1f, a = 1f;

	// Nonessential.
	private int hullLength;
	private int[] edges;
	private float width, height;

	Object rendererObject;

	public MeshAttachment(String name) {
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

	public void computeWorldVertices(float x, float y, Slot slot, float[] worldVertices) {
		Bone bone = slot.getBone();
		x += bone.getWorldX();
		y += bone.getWorldY();
		float m00 = bone.getM00(), m01 = bone.getM01(), m10 = bone.getM10(), m11 = bone.getM11();
		float[] vertices = this.vertices;
		if (slot.getAttachmentVerticesCount() == vertices.length)
			vertices = slot.getAttachmentVertices();
		for (int i = 0, n = vertices.length; i < n; i += 2) {
			float vx = vertices[i];
			float vy = vertices[i + 1];
			worldVertices[i] = vx * m00 + vy * m01 + x;
			worldVertices[i + 1] = vx * m10 + vy * m11 + y;
		}
	}

	public float[] getVertices() {
		return vertices;
	}

	public void setVertices(float[] vertices) {
		this.vertices = vertices;
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

	public short[] getTriangles() {
		return triangles;
	}

	public void setTriangles(short[] triangles) {
		this.triangles = triangles;
	}

	public float getR() {
		return r;
	}

	public void setR(float r) {
		this.r = r;
	}

	public float getB() {
		return b;
	}

	public void setB(float b) {
		this.b = b;
	}

	public float getG() {
		return g;
	}

	public void setG(float g) {
		this.g = g;
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

	public Object getRendererObject() {
		return rendererObject;
	}

	public void setRendererObject(Object rendererObject) {
		this.rendererObject = rendererObject;
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
