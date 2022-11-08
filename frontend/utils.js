export function getCanvasAndContext(selector) {
    /** @type {HTMLCanvasElement} */
    const canvas = document.querySelector(selector);
    /** @type {CanvasRenderingContext2D} */
    const context = canvas.getContext('2d');
    context.imageSmoothingEnabled = false;
    return context
}

/**
 * 求 x,使得 b%x==0, x属于整数，且 2<x<a 的最小的值
 * @param a {number}
 * @param b {number}
 * @param f {number} scale factor, if > 1 find upper or if < 1 find lower
 */
export function ddd(a, b, f) {
    if (f > 1) {
        for (let i = Math.ceil(a); i < b; i++) {
            if (b % i === 0) return i
        }
    } else if (f < 1) {
        for (let i = Math.ceil(a); i > 2; i--) {
            if (b % i === 0) return i
        }
    }
    return -1;
}

export function normalize(n) {
    return n < 0 ? -1 : n > 0 ? 1 : 0
}

/**
 * set the ctx.canvas width and height and set imageSmoothingEnabled to false
 * @param ctx {CanvasRenderingContext2D}
 * @param width {number}
 * @param height {number}
 */
export function setCanvasWindowSize(ctx, width, height) {
    ctx.canvas.width = width;
    ctx.canvas.height = height;
    ctx.imageSmoothingEnabled = false
}

export function debounce(fn, delay) {
    let timeout = null
    return function () {
        clearTimeout(timeout)
        timeout = setTimeout(() => {
            fn.apply(this, arguments)
        }, delay);
    }
}