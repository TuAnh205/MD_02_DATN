import { initializeApp, getApps } from 'firebase/app';
import { getAuth, GoogleAuthProvider, signInWithPopup } from 'firebase/auth';

const firebaseConfig = {
  apiKey: (import.meta.env.VITE_FIREBASE_API_KEY || '').trim(),
  authDomain: (import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || '').trim(),
  projectId: (import.meta.env.VITE_FIREBASE_PROJECT_ID || '').trim(),
  appId: (import.meta.env.VITE_FIREBASE_APP_ID || '').trim(),
};

export const isFirebaseAuthConfigured =
  Boolean(firebaseConfig.apiKey) &&
  Boolean(firebaseConfig.authDomain) &&
  Boolean(firebaseConfig.projectId) &&
  Boolean(firebaseConfig.appId);

let firebaseAuth = null;
let googleProvider = null;

if (isFirebaseAuthConfigured) {
  const app = getApps().length ? getApps()[0] : initializeApp(firebaseConfig);
  firebaseAuth = getAuth(app);
  googleProvider = new GoogleAuthProvider();
  googleProvider.setCustomParameters({ prompt: 'select_account' });
}

export async function signInWithGooglePopup() {
  if (!isFirebaseAuthConfigured || !firebaseAuth || !googleProvider) {
    throw new Error('Firebase Google Auth is not configured');
  }

  const result = await signInWithPopup(firebaseAuth, googleProvider);
  const credential = GoogleAuthProvider.credentialFromResult(result);
  const oauthIdToken = credential?.idToken;
  const idToken = oauthIdToken || (await result.user.getIdToken());

  return {
    idToken,
    email: result.user.email || '',
    name: result.user.displayName || '',
  };
}
