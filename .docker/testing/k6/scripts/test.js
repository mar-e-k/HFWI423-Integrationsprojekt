import http from 'k6/http'
import { check, sleep } from 'k6'

export default function () {
    const data = { username: 'admin.three', password: 'admin' }
    let res = http.post('http://localhost:8080/login/', data)

    check(res, { 'success login': (r) => r.status === 200 })

    sleep(0.3)
}