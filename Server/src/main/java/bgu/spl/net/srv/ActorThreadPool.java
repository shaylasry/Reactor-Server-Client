package bgu.spl.net.srv;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ActorThreadPool {
    //**we use the map to keep pending task for each CH,key will be CH in map and pending task for this CH will be the value
    private final Map<Object, Queue<Runnable>> acts;
    private final ReadWriteLock actsRWLock;
    //**playingNow will be set that contains which CH are now active so we wont enter other Task to submitted task with the same CH(same type)
    //**this will be a concurrentHashMap because we don't want two working thread to check it on the same time and enter two different task of same CH
    private final Set<Object> playingNow;
    //**ExecutorService will be the ThreadPool in charge of the threads
    private final ExecutorService threads;

    public ActorThreadPool(int threads) {
        this.threads = Executors.newFixedThreadPool(threads);
        //**we use WekHashMap so when CH list is empty garbage collector will clean it and we won't need to do it by ourself
        //**because many thread get access to acts and acts is not concurrent hashMap and not thread safe we need to take care concurrence in another place
        acts = new WeakHashMap<>();
        playingNow = ConcurrentHashMap.newKeySet();
        actsRWLock = new ReentrantReadWriteLock();
    }

    //**Main thread(server) will be the first on to call submit from handleReadWrite method
    public void submit(Object act, Runnable r) {
        //**we use synchronized to prevent server and working thread to go to the same CH together(act here is a CH)
        //**this may occur when playingNow contains act in it, which means we add new task from server while working thread already started working on
        //**act pending list
        synchronized (act) {
            //**if CH not in playingNow server will ad it to playingNow and call execute method
            if (!playingNow.contains(act)) {
                playingNow.add(act);
                //**execute method will be submitted by working thread(see implementation)
                execute(r, act);
            } else {
                //**if CH is in playingNoe, the method below put the task in the pending list of one of the CH in acts HashMap
                pendingRunnablesOf(act).add(r);
                //**so to sum it up, if CH wasnt in playingNow, server will ask working thread to do it(to add it to the ExecutorService submitted task list)
                //**and if CH is on playingNow, server will put it in keep in acts hashMap so when the current CH Task in ExecutorService will end
                //**one of the thread will take the next CH task.
            }
        }
    }

    public void shutdown() {
        threads.shutdownNow();
    }
    //**if CH doesn't exist in acts HashMap ,method will add CH to acts HashMap and then
    //**return reference to the CH (act) task pending list in the acts HashMap
    //**it CH exists in acts HashMap, method need only to return reference to the CH (act) task pending list in the acts HashMap
    private Queue<Runnable> pendingRunnablesOf(Object act) {

        actsRWLock.readLock().lock();
        Queue<Runnable> pendingRunnables = acts.get(act);
        actsRWLock.readLock().unlock();

        if (pendingRunnables == null) {
            actsRWLock.writeLock().lock();
            acts.put(act, pendingRunnables = new LinkedList<>());
            actsRWLock.writeLock().unlock();
        }
        return pendingRunnables;
    }
    //**execute will submit by server and in it server will run working thread thread
    private void execute(Runnable r, Object act) {
        //**threads.execute method below is different from the ActorThreadPool execute method
        //**this method will add the command to ExecutorService submitted task list
        //**according to marina in the ExecutorService submitted task list all task happens by order
        threads.execute(() -> {
            //**thread will run the task and no matter if it succeed or not it will call complete method
            //**complete will check if there is any other tasks for curr CH and if there is it will add the top one
            //**to the ExecutorService submitted task list
            try {
                r.run();
            } finally {
                complete(act);
            }
            //**PAY ATTENTION -> in the first submit server will run the execute command.
            //**then working thread starts the try for r.run and complete.
            //**while calling complete if there are more pending tasks for the CH thread will call execute method.
            //**it means that there is a chance that after finish the r.run and complete the next pending task will go
            //**to another thread for the curr CH
            //**also,server and multiple number of thread will try access the sam CH and this is why we use synchronized
            //**for act (CH) in submit and in complete
        });
    }

    private void complete(Object act) {
        synchronized (act) {
            //**takes the pending list of curr CH from acts Hashmap
            Queue<Runnable> pending = pendingRunnablesOf(act);
            if (pending.isEmpty()) {
                //**if no more task waiting remove CH from playingNow
                playingNow.remove(act);
            } else {
                //**use execute to handle the next Task pending for curr CH
                execute(pending.poll(), act);
            }
        }
    }

}
