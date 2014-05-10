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

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.PlayN;
import pythagoras.f.FloatMath;

import com.esotericsoftware.spine.Atlas.AtlasRegion;

/** Attachment that displays a texture region. */
public class RegionAttachment extends Attachment {

	private AtlasRegion region;

	float regionOffsetX, regionOffsetY, regionWidth, regionHeight, regionOriginalWidth, regionOriginalHeight;
	final float[] offset = new float[8], uvs = new float[8];
	float r = 1f, g = 1f, b = 1f, a = 1f;

	String path;
	private ImageLayer rendererObject;
	private boolean rotate;

	public RegionAttachment(String name) {
		super(name);
	}

	public void setRegion(AtlasRegion region) {
		this.regionOffsetX = region.offsetX;
		this.regionOffsetY = region.offsetY;
		this.regionWidth = region.width;
		this.regionHeight = region.height;
		this.regionOriginalWidth = region.originalWidth;
		this.regionOriginalHeight = region.originalHeight;

		Image texture = (Image) region.page.rendererObject;
		if (this.rendererObject == null) {
			this.rendererObject = PlayN.graphics().createImageLayer(texture.subImage(0f, 0f, 0f, 0f));
		} else {
			this.rendererObject.setImage(texture.subImage(0f, 0f, 0f, 0f));
		}

		this.setUVs(region.u, region.v, region.u2, region.v2, region.rotate);
	}

	public AtlasRegion getRegion() {
		if (region == null)
			throw new IllegalStateException("Region has not been set: " + this);
		return region;
	}

	public void setUVs(float u, float v, float u2, float v2, boolean rotate) {
		this.rotate = rotate;
		Image.Region imageRegion = (Image.Region) rendererObject.image();

		float pageWidth = imageRegion.parent().width();
		float pageHeight = imageRegion.parent().height();

		float width = (u2 - u) * pageWidth;
		float height = (v2 - v) * pageHeight;

		float x = u * pageWidth;
		float y = v * pageHeight;

		imageRegion.setBounds( //
				rotate ? x : x, //
				rotate ? y + height : y + height, //
				rotate ? width : width, //
				rotate ? -height : -height //
				);
		rendererObject.setOrigin(Math.abs(imageRegion.width() / 2f), Math.abs(imageRegion.height() / 2f));

		// FIXME
		// float[] uvs = this.uvs;
		// if (rotate) {
		// uvs[X2] = u;
		// uvs[Y2] = v2;
		// uvs[X3] = u;
		// uvs[Y3] = v;
		// uvs[X4] = u2;
		// uvs[Y4] = v;
		// uvs[X1] = u2;
		// uvs[Y1] = v2;
		// } else {
		// uvs[X1] = u;
		// uvs[Y1] = v2;
		// uvs[X2] = u;
		// uvs[Y2] = v;
		// uvs[X3] = u2;
		// uvs[Y3] = v;
		// uvs[X4] = u2;
		// uvs[Y4] = v2;
		// }
	}

	// public static final int X1 = 0;
	// public static final int Y1 = 1;
	// public static final int X2 = 2;
	// public static final int Y2 = 3;
	// public static final int X3 = 4;
	// public static final int Y3 = 5;
	// public static final int X4 = 6;
	// public static final int Y4 = 7;
	//
	// public void updateOffset() {
	// float width = rendererObject.width();
	// float height = rendererObject.height();
	// float scaleX = rendererObject.scaleX();
	// float scaleY = rendererObject.scaleY();
	// float regionScaleX = width / regionOriginalWidth * scaleX;
	// float regionScaleY = height / regionOriginalHeight * scaleY;
	// float localX = -width / 2 * scaleX + regionOffsetX * regionScaleX;
	// float localY = -height / 2 * scaleY + regionOffsetY * regionScaleY;
	// float localX2 = localX + regionWidth * regionScaleX;
	// float localY2 = localY + regionHeight * regionScaleY;
	// float radians = rendererObject.rotation();
	// float cos = (float) Math.cos(radians);
	// float sin = (float) Math.sin(radians);
	// float x = rendererObject.tx();
	// float y = rendererObject.ty();
	// float localXCos = localX * cos + x;
	// float localXSin = localX * sin;
	// float localYCos = localY * cos + y;
	// float localYSin = localY * sin;
	// float localX2Cos = localX2 * cos + x;
	// float localX2Sin = localX2 * sin;
	// float localY2Cos = localY2 * cos + y;
	// float localY2Sin = localY2 * sin;
	// float[] offset = this.offset;
	// offset[X1] = localXCos - localYSin;
	// offset[Y1] = localYCos + localXSin;
	// offset[X2] = localXCos - localY2Sin;
	// offset[Y2] = localY2Cos + localXSin;
	// offset[X3] = localX2Cos - localY2Sin;
	// offset[Y3] = localY2Cos + localX2Sin;
	// offset[X4] = localX2Cos - localYSin;
	// offset[Y4] = localYCos + localX2Sin;
	// }

	// FIXME
	// public void computeWorldVertices(float x, float y, Bone bone, float[] worldVertices) {
	// x += bone.getWorldX();
	// y += bone.getWorldY();
	// float m00 = bone.getM00(), m01 = bone.getM01(), m10 = bone.getM10(), m11 = bone.getM11();
	// float[] offset = this.offset;
	// worldVertices[X1] = offset[X1] * m00 + offset[Y1] * m01 + x;
	// worldVertices[Y1] = offset[X1] * m10 + offset[Y1] * m11 + y;
	// worldVertices[X2] = offset[X2] * m00 + offset[Y2] * m01 + x;
	// worldVertices[Y2] = offset[X2] * m10 + offset[Y2] * m11 + y;
	// worldVertices[X3] = offset[X3] * m00 + offset[Y3] * m01 + x;
	// worldVertices[Y3] = offset[X3] * m10 + offset[Y3] * m11 + y;
	// worldVertices[X4] = offset[X4] * m00 + offset[Y4] * m01 + x;
	// worldVertices[Y4] = offset[X4] * m10 + offset[Y4] * m11 + y;
	// }

	public float getX() {
		return rendererObject.tx();
	}

	public void setX(float x) {
		rendererObject.setTx(x);
	}

	public float getY() {
		return rendererObject.ty();
	}

	public void setY(float y) {
		rendererObject.setTy(y);
	}

	public float getRotation() {
		return FloatMath.toDegrees(rendererObject.rotation());
	}

	public void setRotation(float degrees) {
		rendererObject.setRotation(FloatMath.toRadians(degrees + (rotate ? -90f : 0f)));
	}

	public float getScaleX() {
		return rendererObject.scaleX();
	}

	public void setScaleX(float scaleX) {
		rendererObject.setScaleX(scaleX);
	}

	public float getScaleY() {
		return rendererObject.scaleY();
	}

	public void setScaleY(float scaleY) {
		rendererObject.setScaleY(scaleY);
	}

	public float getWidth() {
		return rendererObject.width();
	}

	public void setWidth(float width) {
		if (rotate)
			rendererObject.setHeight(width);
		else
			rendererObject.setWidth(width);
	}

	public float getHeight() {
		return rendererObject.height();
	}

	public void setHeight(float height) {
		if (rotate)
			rendererObject.setWidth(height);
		else
			rendererObject.setHeight(height);
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

	public ImageLayer getRendererObject() {
		return rendererObject;
	}

	public void setRendererObject(ImageLayer rendererObject) {
		this.rendererObject = rendererObject;
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

	public float[] getOffset() {
		return offset;
	}

	public float[] getUvs() {
		return uvs;
	}

}