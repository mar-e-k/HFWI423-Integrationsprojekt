import { REGISTER_IDS, CASHIER_IDS, PAYMENT_METHODS, ARTICLE_GTINS } from './config.js';

export const pickRandom = (arr) => arr[Math.floor(Math.random() * arr.length)];
export const pickRegisterId = () => pickRandom(REGISTER_IDS);
export const pickCashierId = (vuId) => CASHIER_IDS[(vuId - 1) % CASHIER_IDS.length];
export const pickPaymentMethod = () => pickRandom(PAYMENT_METHODS);
export const pickGtin = () => pickRandom(ARTICLE_GTINS);