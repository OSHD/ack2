/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the license, or (at your option) any later version.
 */
package com.runetek.client;

import com.runetek.services.RSService;

/**
 * An abstract wrapping mechanism for arbitrary data flow between
 * this adapter and the providing anonymous RS delegate, that can
 * be naturally expanded to provide non-standard functionality of
 * delegate. An Adapter is purely an observer of its peer, and hold
 * a strong reference to the peer for simplicity sake. A adapter may
 * also request that the peer notify the adapter of any events that
 * occur within the peer using a {@link org.runetek.client.listener.RSListener}
 * object to serve as the bridge between peer to adapter interaction,
 * if the peer is capable of such functionality. This framework allows for a
 * single interface for adapters to read upon (the delegate/peer) and N interfaces
 * to allow the peer to interact with N adapter-that request an observable
 * functionality. Any and all events will be bridged through the listener.
 *
 * Any and all API implementations of adapters should be thought of
 * as utility and ease-of-use based objects, where they are never required to
 * utilize a delegate or peer, and one may freely build any adapter based
 * implementation to utilize the peer.
 *
 * All peers have a standard abstraction that a global adapter
 * must be provided for all peers. This adapter is a standard adapter,
 * and will remain in memory throughout the lifetime of peer. This adapter
 * is for optimization sake since the API heavily utilizes an adapted object
 * over the raw low-level peer. Creating a new adapter every time will notably pollute
 * the heap with unused or one-time-use objects. To counter this the api
 * will not create a new adapter every time, but instead use and return
 * the reference to the global adapter. Because this adapter is single
 * instance, the ability to unbind the global adapter is disabled,
 * and any and all attempts to release any listeners bounds to its
 * peer will be voided.
 *
 * If the client
 * was to destroy or nullify any refernces to the peer-which this adapter
 * utilizes-then this adapter is equally useless in functionality, as no
 * field changes will occur. In addition, if the peer was in-explicitly
 * finalized, and this adapter has any observer-type functions those observers
 * would also-normally-enter a dormant like phase where they will most likely
 * never be called. An example of this behavior is adding a mouse listeners
 * to a component, and then destroying that component. The mouse listener will
 * be blind to the fact that its producer is destroyed, and enter a dormant-like
 * phase where it'll never be invoked for the rest of its lifetime.
 * <p>
 * Adapters are deemed as high-level code, while its respected
 * peer is the lowest form of client interaction. Peers are
 * thought of as a native interface, and are sometimes referred
 * to as 'client-native'.
 * <p>
 * <p>
 * The generic peer type defined is to be present only in cases that
 * the adapter has a sub-class in which its peer-type also extends
 * the super classes generic type. The generic augment should be
 * an extending peer of the adapters type (? extends THIS-PEER).
 * The generic type is to be absent in cases that adapters peer type
 * has no internal extensions. // <--- TODO more refined clarity
 * <p>
 * Example:
 * - RSCharacter is extended by RSPlayer internally.
 * - RSPlayer has no extending classes.
 * thus:
 * class Character<A extends RSCharacter> extends Renderable<A> { ... }
 * class Player extends Character<RSPlayer> { ... }
 * ....
 * @author Brainfree
 */
public abstract class Adapter<P extends RSService> {

    /**
     * The providing native referent that we are adapting *
     */
    protected final P peer;

    protected Adapter(P peer) {
        this.peer = peer;
    }
}



