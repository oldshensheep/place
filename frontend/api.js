import {API_SERVER} from "./config.js";


/**
 *
 * @param x {number}
 * @param y {number}
 * @param color {number[]}
 */
export function putPixel(x, y, color) {
    if (!Number.isInteger(x) || !Number.isInteger(y)) {
        throw new Error(`Invalid  pixel value: ${x} and ${y} must be integers`)
    }
    return fetch(`${API_SERVER}/pixels`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            x: x,
            y: y,
            color: color
        })
    })
}

export async function getImageBitmap() {
    const resp = await fetch(`${API_SERVER}/pixels/all`)
    const data = await (await resp.blob()).arrayBuffer();
    return new Uint8ClampedArray(data);
}

export function pixelEventSource() {
    return new EventSource(`${API_SERVER}/time`)
}
