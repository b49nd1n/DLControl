package ru.mgvk.dlcontrol;

/**
 * Класс функций. Стандартные функции: dimmer, shutter, rgb-палитра, pan-tilt,
 * color,gobo,strobo. Новые - добавляются через Function.add()
 * 
 * 
 */
public class Function {

	// Константы
	int DIMMER = 0, SHUTTER = 1, RGB = 2, GOBO = 3, COLOUR = 4, PANTILT = 5;

	//Переменные
	int[] functmas;
	
	public Function() {

	}

	void add(int ftype) {
		if (ftype == DIMMER) {

		}
		if (ftype == SHUTTER) {

		}
		if (ftype == RGB) {

		}
		if (ftype == GOBO) {

		}
		if (ftype == COLOUR) {

		}
		if (ftype == PANTILT) {

		}
		

	}

	void addBtns() {

	}

	void addFaders() {

	}

	public class Basic extends Function {
		Basic() {

		}

		void shutter() {

		}

		void dimmer() {

		}

		void RGB() {

		}

		void colour() {

		}

		void gobo() {

		}

		void PanTilt() {

		}

	}

	public class Special extends Function {
		public Special() {

		}

	}

}