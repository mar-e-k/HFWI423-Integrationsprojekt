import exec from 'k6/execution';
import {PAYMENT_METHODS} from './config.js';

export const randomBetween = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;

export const pickRandom = (arr) => (arr && arr.length > 0) ? arr[Math.floor(Math.random() * arr.length)] : null;

export const pickRegisterId = (registerIds) => pickRandom(registerIds || [1]);

export const pickCashierUuid = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = Math.random() * 16 | 0;
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
};

export const pickPaymentMethod = () => pickRandom(PAYMENT_METHODS);

export const pickGtin = (gtinPool) => pickRandom(gtinPool);

export const generateUniqueId = () => {
    const vuId = exec.vu ? exec.vu.idInTest : 1;
    const iteration = exec.vu ? exec.vu.iterationInInstance : 0;
    const epochTime = Date.now() % 100000000;

    return (epochTime * 1000000) + ((vuId % 1000) * 1000) + (iteration % 1000);
};