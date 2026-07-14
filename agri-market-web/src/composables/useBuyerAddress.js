import { ref } from 'vue'
import { addressApi } from '../api'
import userStore from '../stores/user'

/** 下单表单与个人中心收货地址同步 */
export function useBuyerAddress() {
  const addresses = ref([])
  const selectedAddressId = ref(null)
  const addressLoading = ref(false)

  const formatLabel = (addr) => {
    if (!addr) return ''
    const tag = addr.isDefault ? '【默认】' : ''
    return `${tag}${addr.name} ${addr.phone} · ${addr.address}`
  }

  const applyToBuyer = (buyer, addr) => {
    if (!addr) return
    buyer.name = addr.name || ''
    buyer.phone = addr.phone || ''
    buyer.address = addr.address || ''
  }

  const pickDefault = (list) => {
    if (!list?.length) return null
    return list.find((a) => a.isDefault) || list[0]
  }

  /** 加载地址簿，并预填到 buyer（name/phone/address） */
  const loadAndPrefill = async (buyer) => {
    addressLoading.value = true
    selectedAddressId.value = null
    try {
      const list = (await addressApi.list()) || []
      addresses.value = list
      const def = pickDefault(list)
      if (def) {
        selectedAddressId.value = def.id
        applyToBuyer(buyer, def)
      } else {
        buyer.name = userStore.user?.nickname || buyer.name || ''
        buyer.phone = userStore.user?.phone || buyer.phone || ''
        if (!buyer.address) buyer.address = userStore.user?.address || ''
      }
      return list
    } finally {
      addressLoading.value = false
    }
  }

  const onSelectAddress = (id, buyer) => {
    selectedAddressId.value = id
    const addr = addresses.value.find((a) => a.id === id)
    applyToBuyer(buyer, addr)
  }

  return {
    addresses,
    selectedAddressId,
    addressLoading,
    formatLabel,
    loadAndPrefill,
    onSelectAddress
  }
}
