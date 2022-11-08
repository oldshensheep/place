import {debounce} from "./utils.js"

export class ColorPicker {

    static colorReg = new RegExp(/#[0-9a-f]{8}/g);
    elements = {
        inputElement: null,
        colorElement: null,
        alphaElement: null,
    }

    callback = null
    d = debounce(this.#saveToStorage, 300)

    constructor(color, alpha, input) {
        this.elements.colorElement = color;
        this.elements.alphaElement = alpha;
        this.elements.inputElement = input;
        this.#init();
    }

    setCallback(callback) {
        this.callback = callback
        this.onUpdate()
    }

    /**
     *
     * @param color {string} eg. `#ccddeeff`
     */
    setColor(color) {
        this.elements.colorElement.value = color.slice(0, 7)
        this.elements.alphaElement.value = parseInt(color.slice(7), 16)
        this.#call()
    }


    onUpdate() {
        this.#call()
        this.elements.inputElement.value = this.getColorHex()
        this.d()
    }

    #init() {
        if (localStorage.getItem("color") === null) {
            localStorage.setItem("color", "#ccddeeff")
        }
        const item = localStorage.getItem("color");
        this.setColor(item)

        this.elements.colorElement.addEventListener('input', (e) => {
            this.onUpdate()
        })
        this.elements.alphaElement.addEventListener('input', (e) => {
            this.onUpdate()
        })
        this.elements.inputElement.addEventListener("input", (e) => {
            const value = "#" + e.target.value;
            if (ColorPicker.colorReg.test(value)) {
                this.setColor(value)
            } else {
                console.log(value)
            }
        })
    }

    getColorArray() {
        const [c, t] = this.getElementValue()
        const v = parseInt(c.slice(1), 16)
        return [v >> 16 & 255, v >> 8 & 255, v & 255, parseInt(t)]
    }

    getColorHex() {
        const [c, t] = this.getElementValue()
        return `${c.slice(1)}${parseInt(t, 10).toString(16).padStart(2, "0")}`
    }

    getElementValue() {
        return [this.elements.colorElement.value, this.elements.alphaElement.value]
    }

    #call() {
        if (this.callback !== null) {
            this.callback.call()
        }
    }

    #saveToStorage() {
        localStorage.setItem("color", "#" + this.getColorHex())
    }
}