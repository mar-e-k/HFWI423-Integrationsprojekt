import {pickRegisterId, pickCashierUuid, sleepBetween} from '../utils.js';
import {checkout, cancelReceipt, printReceipt, depositReturn} from '../client/receipt.js';
import {createCheckAndRedeemVoucher} from '../client/voucher.js';

export function runFullBon(token, pools, opts = {}) {
    const registerId = pickRegisterId();
    const cashierId = pickCashierUuid();
    const depositChance = opts.depositChance ?? 0.25;
    const cancelChance = opts.cancelChance ?? 0.01;
    const voucherChance = opts.voucherChance ?? 0.10;
    const stepPaceMinMs = opts.stepPaceMinMs ?? 0;
    const stepPaceMaxMs = opts.stepPaceMaxMs ?? stepPaceMinMs;
    const paceStep = () => sleepBetween(stepPaceMinMs, stepPaceMaxMs);

    const receiptId = checkout(token, registerId, cashierId, pools, opts);
    if (!receiptId) return false;

    if (Math.random() < cancelChance) {
        paceStep();
        cancelReceipt(token, receiptId);
        return true;
    }

    if (Math.random() < voucherChance) {
        paceStep();
        createCheckAndRedeemVoucher(token);
    }

    if (Math.random() < depositChance) {
        paceStep();
        depositReturn(token, registerId, cashierId, pools.depositPool);
    }

    paceStep();
    printReceipt(token, receiptId);
    return true;
}
