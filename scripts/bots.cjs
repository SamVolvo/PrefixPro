const mc = require('minecraft-protocol')
const port = Number(process.argv[2])
const version = process.argv[3]
const clients = ['PrefixTester', 'PrefixViewer'].map(username => {
  const client = mc.createClient({ host: '127.0.0.1', port, username, auth: 'offline', version })
  client.on('login', () => console.log('LOGIN ' + username))
  client.on('playerJoin', () => console.log('PLAY ' + username))
  client.on('position', packet => {
    if (packet.teleportId !== undefined) client.write('teleport_confirm', { teleportId: packet.teleportId })
  })
  client.on('error', error => console.error(username + ': ' + error.message))
  client.on('kicked', reason => console.error('KICK ' + username + ': ' + JSON.stringify(reason)))
  return client
})
process.on('SIGTERM', () => { clients.forEach(c => c.end()); process.exit(0) })
