/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bit101.components
{
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.BlendMode;
	import flash.display.DisplayObject;
	import flash.display.DisplayObjectContainer;
	import flash.display.GradientType;
	import flash.display.Graphics;
	import flash.display.InterpolationMethod;
	import flash.display.SpreadMethod;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.geom.Matrix;
	
	public class ColorChooser extends Component
	{
		private var _input:InputText;
		private var _swatch:Sprite;
		private var _value:uint = 0xff0000;

		public static const TOP:String = "top";
		public static const BOTTOM:String = "bottom";
		
		private var _usePopup:Boolean = false;
		private var _model:DisplayObject;
		private var _defaultModelColors:Array=[0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x0000FF, 0xFF00FF, 0xFF0000,0xFFFFFF,0x000000];
		private var _colors:BitmapData;
		private var _colorsContainer:Sprite;
		private var _popupAlign:String = BOTTOM;
		private var _tmpColorChoice:uint = _value;
		private var _oldColorChoice:uint = _value;
		
		
		/**
		 * Constructor
		 * @param parent The parent DisplayObjectContainer on which to add this ColorChooser.
		 * @param xpos The x position to place this component.
		 * @param ypos The y position to place this component.
		 * @param value The initial color value of this component.
		 * @param defaultHandler The event handling function to handle the default event for this component (change in this case).
		 */
		
		public function ColorChooser(parent:DisplayObjectContainer = null, xpos:Number = 0, ypos:Number =  0, value:uint = 0xff0000, defaultHandler:Function = null)
		{
			_value = value;		
			_oldColorChoice = _tmpColorChoice = _value;
			
			super(parent, xpos, ypos);
			
			if(defaultHandler != null)
			{
				addEventListener(Event.CHANGE, defaultHandler);
			}
				
		}		
		
		/**
		 * Initializes the component.
		 */
		override protected function init():void
		{
			
			super.init();

			_width = 65;
			_height = 15;
			value = _value;
		}
		
		override protected function addChildren():void
		{
			_input = new InputText();
			_input.width = 45;
			_input.restrict = "0123456789ABCDEFabcdef";
			_input.maxChars = 6;
			addChild(_input);
			_input.addEventListener(Event.CHANGE, onChange);
			
			_swatch = new Sprite();
			_swatch.x = 50;
			_swatch.filters = [getShadow(2, true)];
			addChild(_swatch);
			
			_colorsContainer = new Sprite();
			_colorsContainer.addEventListener(Event.ADDED_TO_STAGE, onColorsAddedToStage);
			_colorsContainer.addEventListener(Event.REMOVED_FROM_STAGE, onColorsRemovedFromStage);
			_model = getDefaultModel();
			drawColors(_model);
		}
		
		///////////////////////////////////
		// public methods
		///////////////////////////////////
		
		/**
		 * Draws the visual ui of the component.
		 */
		override public function draw():void
		{
			super.draw();
			_swatch.graphics.clear();
			_swatch.graphics.beginFill(_value);
			_swatch.graphics.drawRect(0, 0, 16, 16);
			_swatch.graphics.endFill();
		}
		
		///////////////////////////////////
		// event handlers
		///////////////////////////////////
		
		/**
		 * Internal change handler.
		 * @param event The Event passed by the system.
		 */
		protected function onChange(event:Event):void
		{
			event.stopImmediatePropagation();
			_value = parseInt("0x" + _input.text, 16);
			_input.text = _input.text.toUpperCase();
			_oldColorChoice = value;
			invalidate();
			dispatchEvent(new Event(Event.CHANGE));
			
		}	
		
		///////////////////////////////////
		// getter/setters
		///////////////////////////////////
		
		/**
		 * Gets / sets the color value of this ColorChooser.
		 */
		public function set value(n:uint):void
		{
			var str:String = n.toString(16).toUpperCase();
			while(str.length < 6)
			{
				str = "0" + str;
			}
			_input.text = str;
			_value = parseInt("0x" + _input.text, 16);
			invalidate();
		}
		public function get value():uint
		{
			return _value;
		}
		
		///////////////////////////////////
		// COLOR PICKER MODE SUPPORT
		///////////////////////////////////}
		
		
		public function get model():DisplayObject { return _model; }
		public function set model(value:DisplayObject):void 
		{
			_model = value;
			if (_model!=null) {
				drawColors(_model);
				if (!usePopup) usePopup = true;
			} else {
				_model = getDefaultModel();
				drawColors(_model);
				usePopup = false;
			}
		}
		
		private function drawColors(d:DisplayObject):void{
			_colors = new BitmapData(d.width, d.height);
			_colors.draw(d);
			while (_colorsContainer.numChildren) _colorsContainer.removeChildAt(0);
			_colorsContainer.addChild(new Bitmap(_colors));
			placeColors();
		}
		
		public function get popupAlign():String { return _popupAlign; }
		public function set popupAlign(value:String):void {
			_popupAlign = value;
			placeColors();
		}
		
		public function get usePopup():Boolean { return _usePopup; }
		public function set usePopup(value:Boolean):void {
			_usePopup = value;
			
			_swatch.buttonMode = true;
			_colorsContainer.buttonMode = true;
			_colorsContainer.addEventListener(MouseEvent.MOUSE_MOVE, browseColorChoice);
			_colorsContainer.addEventListener(MouseEvent.MOUSE_OUT, backToColorChoice);
			_colorsContainer.addEventListener(MouseEvent.CLICK, setColorChoice);
			_swatch.addEventListener(MouseEvent.CLICK, onSwatchClick);
			
			if (!_usePopup) {
				_swatch.buttonMode = false;
				_colorsContainer.buttonMode = false;
				_colorsContainer.removeEventListener(MouseEvent.MOUSE_MOVE, browseColorChoice);
				_colorsContainer.removeEventListener(MouseEvent.MOUSE_OUT, backToColorChoice);
				_colorsContainer.removeEventListener(MouseEvent.CLICK, setColorChoice);
				_swatch.removeEventListener(MouseEvent.CLICK, onSwatchClick);
			}
		}
		
		/**
		 * The color picker mode Handlers 
		 */
		
		private function onColorsRemovedFromStage(e:Event):void {
			stage.removeEventListener(MouseEvent.CLICK, onStageClick);
		}
		
		private function onColorsAddedToStage(e:Event):void {
			stage.addEventListener(MouseEvent.CLICK, onStageClick);
		}
		
		private function onStageClick(e:MouseEvent):void {
			displayColors();
		}
		 
		
		private function onSwatchClick(event:MouseEvent):void 
		{
			event.stopImmediatePropagation();
			displayColors();
		}
		
		private function backToColorChoice(e:MouseEvent):void 
		{
			value = _oldColorChoice;
		}
		
		private function setColorChoice(e:MouseEvent):void {
			value = _colors.getPixel(_colorsContainer.mouseX, _colorsContainer.mouseY);
			_oldColorChoice = value;
			dispatchEvent(new Event(Event.CHANGE));
			displayColors();
		}
		
		private function browseColorChoice(e:MouseEvent):void 
		{
			_tmpColorChoice = _colors.getPixel(_colorsContainer.mouseX, _colorsContainer.mouseY);
			value = _tmpColorChoice;
		}

		/**
		 * The color picker mode Display functions
		 */
		
		private function displayColors():void 
		{
			if (_colorsContainer.parent) _colorsContainer.parent.removeChild(_colorsContainer);
			else stage.addChild(_colorsContainer);
		}		
		
		private function placeColors():void{
			switch (_popupAlign) 
			{
				case TOP : 
					_colorsContainer.x = x;
					_colorsContainer.y = y-_colorsContainer.height - 4;
				break;
				case BOTTOM : 
					_colorsContainer.x =x;
					_colorsContainer.y = y+22;
				break;
				default: 
					_colorsContainer.x = x;
					_colorsContainer.y = y+22;
				break;
			}
		}
		
		/**
		 * Create the default gradient Model
		 */

		private function getDefaultModel():Sprite {	
			var w:Number = 100;
			var h:Number = 100;
			var bmd:BitmapData = new BitmapData(w, h);
			
			var g1:Sprite = getGradientSprite(w, h, _defaultModelColors);
			bmd.draw(g1);
					
			var blendmodes:Array = [BlendMode.MULTIPLY,BlendMode.ADD];
			var nb:int = blendmodes.length;
			var g2:Sprite = getGradientSprite(h/nb, w, [0xFFFFFF, 0x000000]);		
			
			for (var i:int = 0; i < nb; i++) {
				var blendmode:String = blendmodes[i];
				var m:Matrix = new Matrix();
				m.rotate(-Math.PI / 2);
				m.translate(0, h / nb * i + h/nb);
				bmd.draw(g2, m, null,blendmode);
			}
			
			var s:Sprite = new Sprite();
			var bm:Bitmap = new Bitmap(bmd);
			s.addChild(bm);
			return(s);
		}
		
		private function getGradientSprite(w:Number, h:Number, ca:Array):Sprite 
		{
			var gc:Array = ca;
			var gs:Sprite = new Sprite();
			var g:Graphics = gs.graphics;
			var gn:int = gc.length;
			var ga:Array = [];
			var gr:Array = [];
			var gm:Matrix = new Matrix(); gm.createGradientBox(w, h, 0, 0, 0);
			for (var i:int = 0; i < gn; i++) { ga.push(1); gr.push(0x00 + 0xFF / (gn - 1) * i); }
			g.beginGradientFill(GradientType.LINEAR, gc, ga, gr, gm, SpreadMethod.PAD,InterpolationMethod.RGB);
			g.drawRect(0, 0, w, h);
			g.endFill();	
			return(gs);
		}
	}
}