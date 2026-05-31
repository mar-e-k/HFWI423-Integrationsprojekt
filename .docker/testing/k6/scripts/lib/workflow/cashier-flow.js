import {pickRegisterId, pickRandom, pickCashierUuid} from '../utils.js';
import {checkout, cancelReceipt, printReceipt, depositReturn} from '../client/receipt.js';
import {checkAndRedeemVoucher} from '../client/voucher.js';

export function runFullBon(token, pools, opts = {}) {
    const registerId = pickRegisterId();
    const cashierId = pickCashierUuid();
    const depositChance = opts.depositChance ?? 0.25;
    const cancelChance = opts.cancelChance ?? 0.01;
    const voucherChance = opts.voucherChance ?? 0.10;

    const receiptId = checkout(token, registerId, cashierId, pools, opts);
    if (!receiptId) return false;

    if (Math.random() < cancelChance) {
        cancelReceipt(token, receiptId);
        return true;
    }

    if (Math.random() < voucherChance && pools.gtinPool?.length) {
        checkAndRedeemVoucher(token, pickRandom(pools.gtinPool));
    }

    if (Math.random() < depositChance) {
        depositReturn(token, registerId, cashierId, pools.depositPool);
    }

    printReceipt(token, receiptId);
    return true;
}