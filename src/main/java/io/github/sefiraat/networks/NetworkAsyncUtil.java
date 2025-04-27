package io.github.sefiraat.networks;

import com.google.common.base.Preconditions;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.tasks.TickerTask;
import lombok.Getter;

import me.matl114.matlib.algorithms.algorithm.ExecutorUtils;
import me.matl114.matlib.algorithms.designs.concurrency.FixedWorkerBatchExecutor;
import me.matl114.matlib.algorithms.designs.concurrency.ObjectLockFactory;
import me.matl114.matlib.core.Manager;
import me.matl114.matlib.utils.reflect.proxy.ProxyBuilder;
import me.matl114.matlib.utils.reflect.proxy.invocation.AdaptorInvocation;
import me.matl114.matlib.utils.reflect.wrapper.MethodAccess;

import me.matl114.matlibAdaptor.algorithms.dataStructures.LockFactory;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class NetworkAsyncUtil implements Manager {
    @Getter
    private static NetworkAsyncUtil instance;
    @Getter
    private static FixedWorkerBatchExecutor fixedParallelWorker;
    private Plugin plugin;
    public NetworkAsyncUtil() {
        instance = this;
    }
    //private BukkitTask cleanTask ;
    public NetworkAsyncUtil init(Plugin plugin,String... args){
        this.plugin = plugin;
        Networks.getInstance().getLogger().info("Enabling Network Async Util");

        boolean isAsync ;
        try{
            TickerTask ticker = Slimefun.getTickerTask();
            Class<?> tickerAsyncClass = ticker.getClass();
            Field asyncField = tickerAsyncClass.getDeclaredField("useAsync");
            asyncField.setAccessible(true);
            isAsync = (boolean) asyncField.get(ticker);
        }catch (Throwable e){
            isAsync = false;
        }
        rootLockFactory = new ObjectLockFactory<>(Location.class, Location::clone);
        if(isAsync){
            useAsync = true;
            this.fixedParallelWorker = new FixedWorkerBatchExecutor(4, 4096);
            Networks.getInstance().getLogger().info("Async Ticker Task Detected,Enabling Parallel Running Protector");
            try{
                Object lockFactory = MethodAccess.ofName(Slimefun.class,"getCargoLockFactory")
                        .noSnapShot()
                        .initWithNull()
                        .invoke(null);
                cargoLockFactory = ProxyBuilder.buildMatlibAdaptorOf(LockFactory.class, lockFactory,(x)-> AdaptorInvocation.createASM(lockFactory.getClass(), x));
                Networks.getInstance().getLogger().info("Slimefun Async Cargo Factory Adaptor created successfully");
            }catch (Throwable error){
                cargoLockFactory = new ObjectLockFactory<>(Location.class,Location::clone)
                        .init(plugin)
                        .setupRefreshTask(10*20*60);
                Networks.getInstance().getLogger().severe("Slimefun Async Cargo Factory not found!");
                Networks.getInstance().getLogger().severe("Creating Network Cargo Lock Factory...");
            }
        }else{
            useAsync = false;
            this.fixedParallelWorker = new FixedWorkerBatchExecutor(6,4096);
            cargoLockFactory = new ObjectLockFactory<>(Location.class,Location::clone);
        }
        Preconditions.checkNotNull(this.cargoLockFactory);
//        Preconditions.checkNotNull(getParallelExecutor());
        Preconditions.checkNotNull(this.fixedParallelWorker);
        Preconditions.checkNotNull(this.rootLockFactory);
        this.fixedParallelWorker.startBusy();
        return this;
    }

    @Override
    public NetworkAsyncUtil reload() {
        deconstruct();
        return init(plugin);
    }

    public void deconstruct() {
//        if(parallelExecutor != null){
//            parallelExecutor.shutdown();
//        }
        Networks.getInstance().getLogger().info("Disabling Network Async Util");
        this.fixedParallelWorker.shutdownNow();
        return;
    }
    //private AbstractExecutorService parallelExecutor;
    @Getter
    private boolean useAsync;
    private AbstractExecutorService genPool(){
        //do we really need this?
//        try{
//            if(useAsync){
//                Class<? extends TickerTask> asyncTickerTask = Slimefun.getTickerTask().getClass();
//                Field executorField = asyncTickerTask.getDeclaredField("tickerThreadPool");
//                executorField.setAccessible(true);
//                AbstractExecutorService service = (AbstractExecutorService) executorField.get(Slimefun.getTickerTask());
//                Preconditions.checkNotNull(service);
//                Networks.getInstance().getLogger().info("Using Slimefun TickerTask Executor Service as thread pool");
//                return service;
//            }
//        }catch (Throwable anything){
//
//        }
        int parallelism =Math.min((Runtime.getRuntime().availableProcessors()/2),8) ;
        ForkJoinPool executor= new ForkJoinPool(parallelism);
        Networks.getInstance().getLogger().info("Starting Thread Pool with parallelism "+parallelism);
//        Networks.getInstance().getLogger().info("Starting Thread Pool using coreSize "+parallelism+" and maxSize "+3*parallelism);
//                    new ThreadPoolExecutor(parallelism,3*parallelism,60, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(5000), new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;

    }
    private void restartPool(){
//        if(parallelExecutor != null){
//            Networks.getInstance().getLogger().info("Restarting Thread Pool");
//            parallelExecutor.shutdown();
//        }
//        parallelExecutor = genPool();
    }

   // private final Map<Location,ReentrantLock> locks = new ConcurrentHashMap<>();
    private LockFactory<Location> cargoLockFactory;
    private LockFactory<Location> rootLockFactory;
    private final ConcurrentHashMap<Location,Semaphore> parallelTaskLock = new ConcurrentHashMap<>();
    public void ensureLocation(Location location,Runnable runnable){
        this.cargoLockFactory.ensureLock(runnable,location);

//        if(useAsync||ExperimentalFeatureManager.getInstance().isEnableNotWaitTillJoin()){
//        }else {
//            runnable.run();
//        }
    }

    // we should use different lock factory for root menus, because actually we don't know which menu will be pushed
    // we can not acquire a lock for root menu when entering the getItemStack/ addItemStack,
    // when acquiring locks from root IO, may cause deadlock-> thread A holds Location A, requesting for root menu B, thread B holds menu B location as a cargo position and trys to require Location A,
    public void ensureRootLocation(Location location, Runnable runnable){
        this.rootLockFactory.ensureLock(runnable, location);
    }
    public <T extends Object> T ensureRootLocation(Location location, Supplier<T> runnable){
        return this.rootLockFactory.ensureLock(runnable, location);
    }

    public <T extends Object> T ensureLocation(Location location, Supplier<T> runnable){
        return this.cargoLockFactory.ensureLock(runnable,location);

//        if(useAsync||ExperimentalFeatureManager.getInstance().isEnableNotWaitTillJoin()){
//        }else {
//            return runnable.get();
//        }
    }
    public Semaphore getParallelTaskLock(Location loc){
        return parallelTaskLock.computeIfAbsent(loc, (k)->new Semaphore(1));
    }
    private static final AtomicInteger taskCounter = new AtomicInteger(0);
//    public Future<Void> submitParallel(Runnable... runnables){
//        return null;
//    }
    public void submitParallel(Runnable task){
        this.fixedParallelWorker.executeAtWorker(task, taskCounter.getAndIncrement());
    }
    public Future<Void> submitParallelFuture(Runnable task){
        FutureTask<Void> future = ExecutorUtils.getFutureTask(task);
        this.fixedParallelWorker.executeAtWorker(future, taskCounter.getAndIncrement());
        return future;
    }
    public void runParallel(){

    }

}
