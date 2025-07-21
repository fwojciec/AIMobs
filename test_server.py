#!/usr/bin/env python3
"""
Simple WebSocket server for testing AI Mobs commands.
Install: pip install websockets
Run: python test_server.py
"""

import asyncio
import websockets
import json
from datetime import datetime

# Connected clients
clients = set()

async def handle_client(websocket):
    """Handle new client connections"""
    print(f"Connection attempt from {websocket.remote_address}")
    clients.add(websocket)
    print(f"Client connected from {websocket.remote_address}")
    
    try:
        # Send a welcome message immediately
        await websocket.send('{"type":"welcome","message":"Connected to AI Mobs server"}')
        print(f"Sent welcome message to {websocket.remote_address}")
        
        async for message in websocket:
            print(f"Received: {message}")
            
    except websockets.exceptions.ConnectionClosed:
        print(f"Client {websocket.remote_address} disconnected")
    except Exception as e:
        print(f"Error handling client {websocket.remote_address}: {e}")
    finally:
        if websocket in clients:
            clients.remove(websocket)

async def send_command(action, parameters=None, context=None):
    """Send a command to all connected clients"""
    if not clients:
        print("No clients connected")
        return
    
    command = {
        "type": "command",
        "timestamp": datetime.now().isoformat() + "Z",
        "data": {
            "action": action,
            "parameters": parameters or {},
            "context": context or {}
        }
    }
    
    message = json.dumps(command)
    print(f"Sending: {message}")
    
    # Send to all clients
    disconnected = []
    for client in clients:
        try:
            await client.send(message)
        except websockets.exceptions.ConnectionClosed:
            disconnected.append(client)
    
    # Clean up disconnected clients
    for client in disconnected:
        clients.remove(client)

async def command_interface():
    """Interactive command interface - non-blocking"""
    print("\n=== AI Mobs Test Server ===")
    print("Available commands:")
    print("  move <x> <y> <z>    - Move to coordinates (spaces or commas)")
    print("  attack <target>     - Attack target")
    print("  collect <item>      - Collect items")
    print("  defend <x> <y> <z>  - Defend area")
    print("  communicate <msg>   - Send message")
    print("  quit                - Exit server")
    print("  status              - Show connected clients")
    print()
    
    import sys
    import select
    
    prompt_shown = False
    
    while True:
        try:
            # Only show prompt once
            if not prompt_shown:
                print("Command: ", end='', flush=True)
                prompt_shown = True
            
            # Check if input is available (non-blocking)
            if sys.stdin in select.select([sys.stdin], [], [], 0.1)[0]:
                cmd = input().strip().split()
                prompt_shown = False  # Reset prompt flag after input
                
                if not cmd:
                    continue
                    
                action = cmd[0].lower()
                
                if action == "quit":
                    break
                elif action == "status":
                    print(f"Connected clients: {len(clients)}")
                elif action == "move" and len(cmd) >= 4:
                    # Handle comma-separated coordinates by cleaning them
                    x_str = cmd[1].rstrip(',')
                    y_str = cmd[2].rstrip(',') 
                    z_str = cmd[3].rstrip(',')
                    await send_command("move", {
                        "x": float(x_str),
                        "y": float(y_str), 
                        "z": float(z_str)
                    })
                elif action == "attack" and len(cmd) >= 2:
                    await send_command("attack", {
                        "target": cmd[1]
                    })
                elif action == "collect" and len(cmd) >= 2:
                    await send_command("collect", {
                        "itemType": cmd[1],
                        "radius": 10,
                        "maxItems": 64
                    })
                elif action == "defend" and len(cmd) >= 4:
                    # Handle comma-separated coordinates by cleaning them
                    x_str = cmd[1].rstrip(',')
                    y_str = cmd[2].rstrip(',') 
                    z_str = cmd[3].rstrip(',')
                    await send_command("defend", {
                        "x": float(x_str),
                        "y": float(y_str),
                        "z": float(z_str),
                        "radius": 10
                    })
                elif action == "communicate" and len(cmd) >= 2:
                    await send_command("communicate", {
                        "message": " ".join(cmd[1:])
                    })
                else:
                    print("Invalid command or missing parameters")
            else:
                # No input available, yield control to event loop
                await asyncio.sleep(0.1)
                
        except KeyboardInterrupt:
            break
        except Exception as e:
            print(f"Error: {e}")
            await asyncio.sleep(0.1)

async def main():
    """Start server and command interface"""
    # Start server with more permissive settings for Java WebSocket client compatibility
    server = await websockets.serve(
        handle_client, 
        "localhost", 
        8080,
        ping_interval=20,
        ping_timeout=10,
        close_timeout=10
    )
    print("WebSocket server started on ws://localhost:8080")
    print("Waiting for connections...")
    
    try:
        await command_interface()
    finally:
        server.close()
        await server.wait_closed()

if __name__ == "__main__":
    asyncio.run(main())