import {ddd} from "./utils.js"

export class Camera {
    state = {
        x: 0,
        y: 0,
        width: 300,
        height: 150
    }
    canvas = {
        width: 300,
        height: 150,
        ratio: 2,
    }

    viewport = {
        width: 300,
        height: 150,
        ratio: 2,
    }

    callback = null

    constructor(canvasWidth, canvasHeight, viewWidth, viewHeight) {
        this.canvas.width = canvasWidth
        this.canvas.height = canvasHeight
        this.canvas.ratio = canvasWidth / canvasHeight
        this.viewport.width = viewWidth
        this.viewport.height = viewHeight
        this.viewport.ratio = viewWidth / viewHeight
    }

    /**
     * scale s then move to (x,y)
     * @param x
     * @param y
     * @param s
     */
    scaleFrom(x, y, s) {
        this.scale(s)
        this.moveTo(x, y);
    }

    /**
     * move the camera center to x,y
     * @param x
     * @param y
     */
    moveTo(x, y) {
        this.state.x = x - this.state.width / 2;
        this.state.y = y - this.state.height / 2;
        this.onUpdate()
    }

    /**
     * move
     * @param x {number}
     * @param y {number}
     */
    move(x, y) {
        this.state.x = x + this.state.x
        this.state.y = y + this.state.y
        this.onUpdate()
    }


    scaleTo(width, height) {
        this.state.width = width;
        this.state.height = height;
        this.onUpdate()
    }

    scale(s) {
        if (s === 1) return;
        this.state.height = Math.ceil(this.state.height * s);

        const ddd1 = ddd(this.state.height, this.canvas.height, s);
        if (ddd1 !== -1) {
            this.state.height = ddd1
        }

        // keep radio
        this.state.width = this.state.height * this.viewport.ratio

        // keep viewport size
        if (this.state.height > this.viewport.height) {
            this.state.height = this.viewport.height
            this.state.width = this.viewport.width
        }
        this.onUpdate()
    }

    onUpdate() {
        this.callback.call(null)
    }

    getCameraCenter() {
        return [this.state.x + this.state.width / 2, this.state.y + this.state.height / 2];
    }

    getScale() {
        return this.state.width / this.viewport.width
    }
}
