import React from 'react';
import {ViewProps, requireNativeComponent} from 'react-native';

type NativeTestViewProps = ViewProps;

const NativeTestView = requireNativeComponent<NativeTestViewProps>('TestView');

export class TestView extends React.Component<NativeTestViewProps> {
  render() {
    return <NativeTestView {...this.props} />;
  }
}
