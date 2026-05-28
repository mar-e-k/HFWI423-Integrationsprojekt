import { pickCashierId, pickRegisterId, pickRandom } from '../utils.js';
import { checkout, cancelReceipt, printReceipt, depositReturn } from '../client/receipt.js';
import { checkAndRedeemVoucher } from '../client/voucher.js';
import { VOUCHER_REGULAR_CODES } from '../config.js';

export function runFullBon(token, vuId, pools, opts = {}) {
    const cashierId = pickCashierId(vuId);
    const registerId = pickRegisterId();
    const depositChance = opts.depositChance ?? 0.25;
    const cancelChance = opts.cancelChance ?? 0.01;
    const voucherChance = opts.voucherChance ?? 0.10;

    const receiptId = checkout(token, registerId, cashierId, pools.articlePool, opts);
    if (!receiptId) return false;

    if (Math.random() < cancelChance) {
        cancelReceipt(token, receiptId);
        return true;
    }

    if (Math.random() < voucherChance && VOUCHER_REGULAR_CODES.length > 0) {
        checkAndRedeemVoucher(token, pickRandom(VOUCHER_REGULAR_CODES));
    }

    if (Math.random() < depositChance) {
        depositReturn(token, registerId, cashierId, pools.depositPool);
    }

    printReceipt(token, receiptId);
    return true;
}