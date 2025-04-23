package io.github.sefiraat.networks;

import com.google.common.base.Preconditions;
import io.github.sefiraat.networks.managers.ExperimentalFeatureManager;
import io.github.thebusybiscuit.slimefun4.core.debug.Debug;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.tasks.TickerTask;
import lombok.Getter;

import me.matl114.matlib.algorithms.designs.concurrency.DefaultLockFactory;
import me.matl114.matlib.algorithms.designs.concurrency.ObjectLockFactory;
import me.matl114.matlib.core.Manager;
import me.matl114.matlib.utils.reflect.proxy.ProxyBuilder;
import me.matl114.matlib.utils.reflect.proxy.invocation.AdaptorInvocation;
import me.matl114.matlib.utils.reflect.wrapper.MethodAccess;

import me.matl114.matlibAdaptor.algorithms.dataStructures.LockFactory;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class NetworkAsyncUtil implements Manager {
    @Getter
    private static NetworkAsyncUtil instance;
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

        if(isAsync){
            useAsync = true;
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
            cargoLockFactory = new ObjectLockFactory<>(Location.class,Location::clone);
        }
        Preconditions.checkNotNull(this.cargoLockFactory);
        Preconditions.checkNotNull(getParallelExecutor());
        return this;
    }

    @Override
    public NetworkAsyncUtil reload() {
        deconstruct();
        return init(plugin);
    }

    public void deconstruct() {
        if(parallelExecutor != null){
            parallelExecutor.shutdown();
        }
        Networks.getInstance().getLogger().info("Disabling Network Async Util");
        return;
    }
    private AbstractExecutorService parallelExecutor;
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
        if(parallelExecutor != null){
            Networks.getInstance().getLogger().info("Restarting Thread Pool");
            parallelExecutor.shutdown();
        }
        parallelExecutor = genPool();
    }

   // private final Map<Location,ReentrantLock> locks = new ConcurrentHashMap<>();
    private LockFactory<Location> cargoLockFactory;
    private final ConcurrentHashMap<Location,Semaphore> parallelTaskLock = new ConcurrentHashMap<>();
    public void ensureLocation(Location location,Runnable runnable){
        this.cargoLockFactory.ensureLock(runnable,location);

//        if(useAsync||ExperimentalFeatureManager.getInstance().isEnableNotWaitTillJoin()){
//        }else {
//            runnable.run();
//        }
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

    public void runParallel(){

    }
    public AbstractExecutorService getParallelExecutor(){
        if(parallelExecutor == null || parallelExecutor.isShutdown()){
            restartPool();
        }
        return parallelExecutor;
    }
}
