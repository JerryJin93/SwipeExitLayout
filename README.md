# SwipeExitLayout

### This project is completely based on androidX.

## How to use

### Step 1: Add it in your root build.gradle at the end of repositories.

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

### Step 2: Add the dependency.

```
	dependencies {
	        implementation 'com.github.JerryJin93:SwipeExitLayout:1.0.2'
	}
```

### Step3:
1. Just extend to the SwipeExitActivity.
```
    public class XxxActivity extends SwipeExitActivity {
        @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
            }
    }
```

2. Use SwipeExitLayout in a specific class.
```
    public abstract class XxxActivity extends AppCompatActivity {
    
        private SwipeExitLayout root;
    
        @SuppressLint("InflateParams")
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Call before setContentView(R.layout.xxx);
            root = (SwipeExitLayout) LayoutInflater.from(this).inflate(R.layout.swipe_exit_container, null);
            root.attachTo(this);
            setContentView(R.layout.activity_xxx);
            
            root.setOnExitListener(new SwipeExitLayout.OnExitListenerImpl() {
                        @Override
                        public void onStart() {
                            super.onStart();
                        }
            
                        @Override
                        public void onExit(int backgroundColor) {
                            super.onExit(backgroundColor);
                        }
            
                        @Override
                        public void onPreFinish() {
                            super.onPreFinish();
                        }
            
                        @Override
                        public void onRestore() {
                            super.onRestore();
                        }
                    });
        }
    
    }
```