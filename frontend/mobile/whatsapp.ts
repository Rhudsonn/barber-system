export function linkWhatsAppBrasileiro(valor?: string | null): string | undefined {
  if (!valor || valor.length > 25 || !/^[+0-9() .-]+$/.test(valor)) return undefined
  let digitos = valor.replace(/\D/g, '')
  if (digitos.startsWith('55') && (digitos.length === 12 || digitos.length === 13)) {
    digitos = digitos.slice(2)
  } else if (digitos.length !== 10 && digitos.length !== 11) {
    return undefined
  }
  if (!/^[1-9][1-9](?:[2-5][0-9]{7}|9[0-9]{8})$/.test(digitos)) return undefined
  return `https://wa.me/55${digitos}`
}
